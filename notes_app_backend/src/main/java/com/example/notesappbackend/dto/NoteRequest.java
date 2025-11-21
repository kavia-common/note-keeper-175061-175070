package com.example.notesappbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO used for creating/updating notes via API requests.
 */
public class NoteRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must be at most 200 characters")
    private String title;

    @Size(max = 10000, message = "Content must be at most 10000 characters")
    private String content;

    /**
     * Optional, comma-separated tags.
     */
    private String tags;

    public NoteRequest() {
    }

    public NoteRequest(String title, String content, String tags) {
        this.title = title;
        this.content = content;
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public NoteRequest setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getContent() {
        return content;
    }

    public NoteRequest setContent(String content) {
        this.content = content;
        return this;
    }

    public String getTags() {
        return tags;
    }

    public NoteRequest setTags(String tags) {
        this.tags = tags;
        return this;
    }
}
