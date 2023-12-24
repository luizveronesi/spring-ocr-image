package dev.luizveronesi.ocr.service.factory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import dev.luizveronesi.ocr.model.OcrType;
import dev.luizveronesi.ocr.service.strategy.OcrStrategy;

@Service
public class OcrServiceFactory {

    private final Map<OcrType, OcrStrategy> strategies = new HashMap<>();

    public OcrServiceFactory(Set<OcrStrategy> strategies) {
        this.createStrategyMap(strategies);
    }

    public OcrStrategy getStrategy(OcrType type) {
        return this.strategies.get(type);
    }

    private void createStrategyMap(Set<OcrStrategy> strategies) {
        strategies.forEach(absenceStrategy -> this.strategies.put(absenceStrategy.getStrategyName(), absenceStrategy));
    }
}