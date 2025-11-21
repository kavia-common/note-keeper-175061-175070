package com.example.notesappbackend.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler standardizes API error responses.
 * Handles NotFoundException (404), validation and parse errors (400), and a generic 500 fallback.
 * Error payload format: {timestamp, status, error, message, path[, fieldErrors]}.
 */
@ControllerAdvice(basePackages = "com.example.notesappbackend")
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * Handle NotFoundException converting it to a 404 response.
     *
     * @param ex      the NotFoundException
     * @param request current HTTP request
     * @return standardized error response
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Object> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        ApiError payload = baseError(HttpStatus.NOT_FOUND, ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(payload);
    }

    /**
     * Handle ConstraintViolationException typically thrown for validation on request params/path variables.
     *
     * @param ex      the validation exception
     * @param request current HTTP request
     * @return standardized error response with fieldErrors map
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        this::violationKey,
                        ConstraintViolation::getMessage,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        ApiError payload = baseError(HttpStatus.BAD_REQUEST, "Validation failed", request.getRequestURI())
                .setFieldErrors(fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(payload);
    }

    /**
     * Override for handling bean validation errors on request bodies (@Valid).
     * Produces a 400 with fieldErrors including field names and messages.
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));

        String path = extractPath(request);
        ApiError payload = baseError(HttpStatus.BAD_REQUEST, "Validation failed", path)
                .setFieldErrors(fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).body(payload);
    }

    /**
     * Override for handling malformed JSON or unreadable request payloads.
     * Produces a 400 with the parsing error message.
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {

        String message = ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage();

        String path = extractPath(request);
        ApiError payload = baseError(HttpStatus.BAD_REQUEST, message, path);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).headers(headers).body(payload);
    }

    /**
     * Fallback handler for uncaught exceptions. Returns 500 Internal Server Error with a generic message.
     *
     * @param ex      the exception
     * @param request current request
     * @return standardized 500 error response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex, HttpServletRequest request) {
        // If Spring generated an ErrorResponseException with a specific status, honor it.
        if (ex instanceof ErrorResponseException errorResponseException) {
            HttpStatus status = errorResponseException.getStatusCode() instanceof HttpStatus httpStatus
                    ? httpStatus
                    : HttpStatus.valueOf(errorResponseException.getStatusCode().value());

            // In Spring 6 / Boot 3.x ErrorResponseException does not expose getReason().
            // Use the problem detail's title/body when available, else fallback to exception message.
            String message = null;
            if (errorResponseException.getBody() != null) {
                if (errorResponseException.getBody().getDetail() != null) {
                    message = errorResponseException.getBody().getDetail();
                } else if (errorResponseException.getBody().getTitle() != null) {
                    message = errorResponseException.getBody().getTitle();
                }
            }
            if (message == null || message.isBlank()) {
                message = ex.getMessage();
            }

            ApiError payload = baseError(status, message, request.getRequestURI());
            return ResponseEntity.status(status).body(payload);
        }

        ApiError payload = baseError(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(payload);
    }

    private ApiError baseError(HttpStatus status, String message, String path) {
        return new ApiError()
                .setTimestamp(Instant.now())
                .setStatus(status.value())
                .setError(status.getReasonPhrase())
                .setMessage(message)
                .setPath(path);
    }

    private String extractPath(WebRequest request) {
        if (request instanceof ServletWebRequest servletWebRequest) {
            return servletWebRequest.getRequest().getRequestURI();
        }
        return "";
    }

    private String violationKey(ConstraintViolation<?> v) {
        // Build a readable key like "parameterName" or "object.field"
        if (v.getPropertyPath() != null) {
            return v.getPropertyPath().toString();
        }
        return "constraint";
    }
}
