package dev.luizveronesi.ocr.service;

import org.springframework.stereotype.Service;

import dev.luizveronesi.ocr.model.OcrRequest;
import dev.luizveronesi.ocr.model.OcrResponse;
import dev.luizveronesi.ocr.service.factory.OcrServiceFactory;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OcrService {

	private final OcrServiceFactory ocrServiceFactory;

	public OcrResponse execute(OcrRequest request) {
		var response = ocrServiceFactory.getStrategy(request.getType()).extract(request);

		response.setFilename(request.getFile().getOriginalFilename());

		return response;
	}
}