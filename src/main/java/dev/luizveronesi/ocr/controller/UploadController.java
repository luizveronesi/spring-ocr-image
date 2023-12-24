package dev.luizveronesi.ocr.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.luizveronesi.ocr.controller.documentation.UploadControllerDocumentation;
import dev.luizveronesi.ocr.model.OcrRequest;
import dev.luizveronesi.ocr.model.OcrResponse;
import dev.luizveronesi.ocr.service.OcrService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class UploadController implements UploadControllerDocumentation {

    private final OcrService uploadService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<OcrResponse> upload(@ModelAttribute OcrRequest request) {
        return new ResponseEntity<>(uploadService.execute(request), HttpStatus.OK);
    }
}
