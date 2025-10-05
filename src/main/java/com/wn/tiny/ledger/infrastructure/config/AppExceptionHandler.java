package com.wn.tiny.ledger.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.wn.tiny.ledger.domain.InvalidTransactionException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class AppExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AppExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, List<String>> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.computeIfAbsent(fieldName, k -> new ArrayList<>()).add(errorMessage);
        });

        var errorResponse = new ErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                "Validation failed for one or more fields",
                request.getRequestURI(),
                errors
        );
        log.error("Validation failed for one or more fields", ex);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(InvalidTransactionException ex, HttpServletRequest request) {
        final HttpStatus status = HttpStatus.BAD_REQUEST;
        var errorResponse = new ErrorResponse(
                Instant.now(),
                status.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        log.error("Transaction exception occurred: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        var errorResponse = new ErrorResponse(
                Instant.now(),
                status.value(),
                "Internal Server Error",
                "An unexpected internal error occurred. Please try again later.",
                request.getRequestURI()
        );
        log.error("An unexpected error occurred at path: {}", request.getRequestURI(), ex);
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFormatException(HttpMessageNotReadableException ex, HttpServletRequest request) {
        final HttpStatus status = HttpStatus.BAD_REQUEST;
        var errorResponse = new ErrorResponse(
                Instant.now(),
                status.value(),
                "Bad Request",
                "Invalid format field" + ex.getMessage(),
                request.getRequestURI()
        );
        log.error("Invalid format field", ex);
        return new ResponseEntity<>(errorResponse, status);
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorResponse(
            Instant timestamp,
            int status,
            String error,
            String message,
            String path,
            Map<String, List<String>> validationErrors
    ) {
        public ErrorResponse(Instant timestamp, int status, String error, String message, String path) {
            this(timestamp, status, error, message, path, null);
        }
    }

}
