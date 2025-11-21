package com.example.notesappbackend.exception;

import java.time.Instant;
import java.util.Map;

/**
 * ApiError represents a standard error payload for API responses.
 * Structure: {timestamp, status, error, message, path, (optional) fieldErrors}
 */
public class ApiError {

    private Instant timestamp;
    private int status;
    private String error;
    private String message;
    private String path;

    /**
     * Optional field errors map used for validation problems.
     * The key is the field name (or constraint), and the value is the validation message.
     */
    private Map<String, String> fieldErrors;

    public ApiError() {
    }

    public ApiError(Instant timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public ApiError setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public ApiError setStatus(int status) {
        this.status = status;
        return this;
    }

    public String getError() {
        return error;
    }

    public ApiError setError(String error) {
        this.error = error;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ApiError setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getPath() {
        return path;
    }

    public ApiError setPath(String path) {
        this.path = path;
        return this;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }

    public ApiError setFieldErrors(Map<String, String> fieldErrors) {
        this.fieldErrors = fieldErrors;
        return this;
    }
}
