package dev.luizveronesi.ocr.model;

import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OcrRequest {

    @NotNull
    private OcrType type;

    @NotNull
    private MultipartFile file;
}
