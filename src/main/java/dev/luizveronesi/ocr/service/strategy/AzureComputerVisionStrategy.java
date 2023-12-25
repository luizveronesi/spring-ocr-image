package dev.luizveronesi.ocr.service.strategy;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVision;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionClient;
import com.microsoft.azure.cognitiveservices.vision.computervision.ComputerVisionManager;
import com.microsoft.azure.cognitiveservices.vision.computervision.implementation.ComputerVisionImpl;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.Line;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.OperationStatusCodes;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadInStreamHeaders;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadInStreamOptionalParameter;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadOperationResult;
import com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadResult;

import dev.luizveronesi.ocr.model.OcrRequest;
import dev.luizveronesi.ocr.model.OcrResponse;
import dev.luizveronesi.ocr.model.OcrType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AzureComputerVisionStrategy implements OcrStrategy {

    private static final Integer LINEBREAK_GAP = 100;

    private static final String LINEBREAK = "\n";

    @Value("${app.azure.computer.vision.subscriptionKey:}")
    private String subscriptionKey;

    @Value("${app.azure.computer.vision.endpoint:}")
    private String endpoint;

    public OcrResponse extract(OcrRequest request) {
        ComputerVisionClient client = this.authenticate();
        ComputerVisionImpl vision = (ComputerVisionImpl) client.computerVision();

        String operationLocation;
        ReadInStreamOptionalParameter params = new ReadInStreamOptionalParameter();
        ReadInStreamHeaders responseHeader;
        try {
            responseHeader = vision.readInStreamWithServiceResponseAsync(
                    request.getFile().getBytes(), params)
                    .toBlocking()
                    .single()
                    .headers();
            operationLocation = responseHeader.operationLocation();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        var result = this.extractResult(vision, operationLocation);

        return OcrResponse.builder()
                .result(result)
                .text(this.extractText(result))
                .build();
    }

    /**
     * Polls for Read result and prints results to console
     * 
     * @param vision Computer Vision instance
     * @return operationLocation returned in the POST Read response header
     */
    private ReadOperationResult extractResult(ComputerVision vision, String operationLocation) {
        String operationId = this.extractOperationIdFromOpLocation(operationLocation);

        boolean pollForResult = true;
        ReadOperationResult readResults = null;

        while (pollForResult) {
            // Poll for result every second
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            readResults = vision.getReadResult(UUID.fromString(operationId));

            // The results will no longer be null when the service has finished processing
            // the request.
            if (readResults != null) {
                // Get request status
                OperationStatusCodes status = readResults.status();

                if (status == OperationStatusCodes.FAILED || status == OperationStatusCodes.SUCCEEDED) {
                    pollForResult = false;
                }
            }
        }

        return readResults;
    }

    private String extractText(ReadOperationResult result) {
        // Print read results, page per page
        Line lastLine = null;
        StringBuilder builder = new StringBuilder();
        for (ReadResult pageResult : result.analyzeResult().readResults()) {
            for (Line line : pageResult.lines()) {
                if (lastLine != null
                        && this.detectLinebreak(lastLine, line)) {
                    builder.append(LINEBREAK);
                }

                builder.append(line.text());
                builder.append(LINEBREAK);

                lastLine = line;
            }
        }
        return builder.toString();

    }

    /**
     * X top left, Y top left, X top right, Y top right, X bottom right, Y bottom
     * right, X bottom left, Y bottom left
     * [31.0, 41.0, 754.0, 45.0, 754.0, 100.0, 30.0, 97.0]
     * [101.0, 110.0, 544.0, 112.0, 544.0, 159.0, 101.0, 157.0]
     * [27.0, 225.0, 843.0, 230.0, 843.0, 288.0, 27.0, 287.0]
     * 
     * Subtract the last element from array: after - before > 100 (fixed)
     */
    private Boolean detectLinebreak(Line before, Line after) {
        var beforeValue = Iterables.getLast(before.boundingBox());
        var afterValue = Iterables.getLast(after.boundingBox());
        return afterValue - beforeValue > LINEBREAK_GAP;
    }

    private String extractOperationIdFromOpLocation(String operationLocation) {
        if (operationLocation != null && !operationLocation.isEmpty()) {
            String[] splits = operationLocation.split("/");

            if (splits != null && splits.length > 0) {
                return splits[splits.length - 1];
            }
        }
        throw new IllegalStateException(
                "Something went wrong: Couldn't extract the operation id from the operation location");
    }

    private ComputerVisionClient authenticate() {
        return ComputerVisionManager.authenticate(subscriptionKey).withEndpoint(endpoint);
    }

    @Override
    public OcrType getStrategyName() {
        return OcrType.AZURE;
    }
}
