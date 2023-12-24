package dev.luizveronesi.ocr.service.strategy;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.textract.AmazonTextract;
import com.amazonaws.services.textract.AmazonTextractClientBuilder;
import com.amazonaws.services.textract.model.AnalyzeDocumentRequest;
import com.amazonaws.services.textract.model.AnalyzeDocumentResult;
import com.amazonaws.services.textract.model.Block;
import com.amazonaws.services.textract.model.Document;

import dev.luizveronesi.ocr.model.OcrRequest;
import dev.luizveronesi.ocr.model.OcrResponse;
import dev.luizveronesi.ocr.model.OcrType;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AwsTextractStrategy implements OcrStrategy {

	private static final String BLOCK_LINE = "LINE";

	@Value("${app.aws.region.static:}")
	private String awsAccountRegion;

	@Value("${app.aws.credentials.access-key:}")
	private String awsCredentialKey;

	@Value("${app.aws.credentials.secret-key:}")
	private String awsCredentialSecret;

	@Value("${app.aws.textract.endpoint:''}")
	private String awsAccountEndpoint;

	public OcrResponse extract(OcrRequest request) {
		Document doc;
		try {
			doc = new Document().withBytes(ByteBuffer.wrap(request.getFile().getBytes()));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		var docRequest = new AnalyzeDocumentRequest()
				.withFeatureTypes("TABLES", "FORMS")
				.withDocument(doc);
		var result = getClient().analyzeDocument(docRequest);

		return OcrResponse.builder()
				.result(result)
				.text(this.extractText(result))
				.build();
	}

	private String extractText(AnalyzeDocumentResult result) {
		List<Block> lines = result.getBlocks().stream().filter(x -> x.getBlockType().equals(BLOCK_LINE))
				.collect(Collectors.toList());
		StringBuffer sb = new StringBuffer();
		for (Block line : lines) {
			sb.append(line.getText());
			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}

	private AmazonTextract getClient() {
		EndpointConfiguration endpoint = new EndpointConfiguration(awsAccountEndpoint, awsAccountRegion);
		return AmazonTextractClientBuilder
				.standard()
				.withEndpointConfiguration(endpoint)
				.withCredentials(this.getCredentialsProvider())
				.build();
	}

	private AWSStaticCredentialsProvider getCredentialsProvider() {
		BasicAWSCredentials credentials = new BasicAWSCredentials(awsCredentialKey, awsCredentialSecret);
		return new AWSStaticCredentialsProvider(credentials);
	}

	@Override
	public OcrType getStrategyName() {
		return OcrType.AWS;
	}
}