package com.example.notesappbackend.exception;

/**
 * NotFoundException represents a 404 Not Found error for missing resources.
 * Throw this exception when a requested entity or resource is not present.
 */
public class NotFoundException extends RuntimeException {

    /**
     * Constructs a NotFoundException with a message.
     *
     * @param message error message describing the missing resource
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs a NotFoundException with a message and the underlying cause.
     *
     * @param message error message describing the missing resource
     * @param cause   underlying cause of the error
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
