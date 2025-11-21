package com.example.notesappbackend;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Basic hello and utility endpoints for the Notes App backend.
 */
@RestController
@Tag(name = "Hello Controller", description = "Basic endpoints for notesappbackend")
public class HelloController {

    // Note: Root ("/") is handled by RootController to redirect to /docs.

    /**
     * PUBLIC_INTERFACE
     * Redirects to Swagger UI, preserving scheme/host/port from incoming request.
     *
     * @param request current HTTP request
     * @return redirect view to Swagger UI
     */
    @GetMapping("/docs")
    @Operation(summary = "API Documentation", description = "Redirects to Swagger UI preserving original scheme/host/port")
    public RedirectView docs(HttpServletRequest request) {
        String target = ServletUriComponentsBuilder
                .fromRequest(request)
                .replacePath("/swagger-ui.html")
                .replaceQuery(null)
                .build()
                .toUriString();

        RedirectView rv = new RedirectView(target);
        rv.setHttp10Compatible(false);
        return rv;
    }

    /**
     * PUBLIC_INTERFACE
     * Simple health check endpoint.
     *
     * @return OK if the app is running
     */
    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns application health status")
    public String health() {
        return "OK";
    }

    /**
     * PUBLIC_INTERFACE
     * Basic application info endpoint.
     *
     * @return application name
     */
    @GetMapping("/api/info")
    @Operation(summary = "Application info", description = "Returns application information")
    public String info() {
        return "Spring Boot Application: notesappbackend";
    }
}
