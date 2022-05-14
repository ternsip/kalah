package com.kalah.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpException.class)
    public ResponseEntity<ExceptionDto> handle(HttpException httpException) {
        ExceptionDto exceptionDto = new ExceptionDto(
                httpException.getMessage()
        );
        return new ResponseEntity<>(exceptionDto, httpException.getStatus());
    }

}