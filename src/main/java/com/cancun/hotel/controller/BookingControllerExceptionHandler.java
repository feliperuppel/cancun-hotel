package com.cancun.hotel.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class BookingControllerExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(BookingControllerExceptionHandler.class);

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<String> badRequestHandler(Exception e){
        return logAndReturn(HttpStatus.BAD_REQUEST, e);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> runtimeExceptionHandler(Exception e){
        return logAndReturn(HttpStatus.INTERNAL_SERVER_ERROR, e);
    }

    private static ResponseEntity<String> logAndReturn(HttpStatus status, Exception e){
        log.error(e.getMessage(), e);
        return ResponseEntity.status(status).body(e.getMessage());
    }
}
