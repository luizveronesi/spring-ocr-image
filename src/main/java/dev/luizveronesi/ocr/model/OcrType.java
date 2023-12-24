package dev.luizveronesi.ocr.model;

public enum OcrType {
	AWS, AZURE;

	public String getName() {
		return this.name();
	}
}
