package com.kalah.rest;

import com.kalah.general.Utils;
import lombok.SneakyThrows;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

@RestController
public class BaseController {

    public static final String MAIN_PAGE = Utils.loadResourceAsString(new File("web/main-page.html"));

    @GetMapping("/")
    public String index() {
        return MAIN_PAGE;
    }

    @GetMapping(
            value = "/image",
            produces = MediaType.IMAGE_PNG_VALUE
    )
    @SneakyThrows
    public ResponseEntity<InputStreamResource> getPngImage(@RequestParam(value = "name") final String name) {
        var image = new ClassPathResource("web/" + name + ".png");
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(new InputStreamResource(image.getInputStream()));
    }

}