package com.example.notesappbackend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import java.time.Instant;

/**
 * NotFoundControllerAdvice provides graceful handling for unmapped routes and methods.
 *
 * Behavior:
 * - If a request results in NoHandlerFoundException (unmapped path), return standardized JSON error for API/media types.
 * - For browser-like requests (Accept includes text/html) at root, redirect to /docs.
 *
 * Note: We are not forcing spring.mvc.throw-exception-if-no-handler-found=true here to keep minimal changes.
 * Spring may route 404s through the default error path; this advice handles framework-raised exceptions.
 */
@ControllerAdvice(basePackages = "com.example.notesappbackend")
public class NotFoundControllerAdvice {

    /**
     * PUBLIC_INTERFACE
     * Handles unmapped routes when Spring throws NoHandlerFoundException.
     * Returns JSON error for non-HTML clients; for HTML at root, redirects to /docs.
     *
     * @param ex      the NoHandlerFoundException
     * @param request the current HTTP request
     * @return either an ApiError or a RedirectView
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handleNoHandler(@NonNull NoHandlerFoundException ex, @NonNull HttpServletRequest request) {
        // If browser is asking HTML for root, send them to docs for a friendlier experience
        if (acceptsHtml(request) && ("/".equals(request.getRequestURI()) || "".equals(request.getRequestURI()))) {
            RedirectView rv = new RedirectView("/docs");
            rv.setHttp10Compatible(false);
            return rv;
        }

        String path = request.getRequestURI();
        HttpStatus status = HttpStatus.NOT_FOUND;

        ApiError payload = new ApiError()
                .setTimestamp(Instant.now())
                .setStatus(status.value())
                .setError(status.getReasonPhrase())
                .setMessage("No handler found for " + ex.getHttpMethod() + " " + ex.getRequestURL())
                .setPath(path);

        return new org.springframework.http.ResponseEntity<>(payload, status);
    }

    /**
     * PUBLIC_INTERFACE
     * Handles method not allowed in a consistent JSON shape.
     *
     * @param ex      HttpRequestMethodNotSupportedException
     * @param request request
     * @return ResponseEntity with ApiError
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object handleMethodNotAllowed(@NonNull HttpRequestMethodNotSupportedException ex, @NonNull HttpServletRequest request) {
        HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
        ApiError payload = new ApiError()
                .setTimestamp(Instant.now())
                .setStatus(status.value())
                .setError(status.getReasonPhrase())
                .setMessage(ex.getMessage())
                .setPath(request.getRequestURI());
        return new org.springframework.http.ResponseEntity<>(payload, status);
    }

    private boolean acceptsHtml(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains(MediaType.TEXT_HTML_VALUE);
    }
}
