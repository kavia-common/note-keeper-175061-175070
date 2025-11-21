package com.example.notesappbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point for the Notes App backend.
 * Ensures a single @SpringBootApplication exists with a consistent, properly-cased name.
 */
// PUBLIC_INTERFACE
@SpringBootApplication
public class NotesAppBackendApplication {

    /**
     * PUBLIC_INTERFACE
     * Main entrypoint used by Spring Boot to launch the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(NotesAppBackendApplication.class, args);
    }
}
