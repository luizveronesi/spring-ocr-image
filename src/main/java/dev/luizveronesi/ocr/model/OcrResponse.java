package dev.luizveronesi.ocr.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OcrResponse {

	// com.amazonaws.services.textract.model.AnalyzeDocumentResult or
	// com.microsoft.azure.cognitiveservices.vision.computervision.models.ReadOperationResult
	private Object result;
	private String text;
	private String filename;
}
