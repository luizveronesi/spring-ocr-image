package dev.luizveronesi.ocr.service.strategy;

import dev.luizveronesi.ocr.model.OcrRequest;
import dev.luizveronesi.ocr.model.OcrResponse;
import dev.luizveronesi.ocr.model.OcrType;
import dev.luizveronesi.ocr.service.factory.StrategyBase;

public interface OcrStrategy extends StrategyBase<OcrType> {

	OcrResponse extract(OcrRequest request);
}
