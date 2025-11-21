package com.example.notesappbackend.service;

import com.example.notesappbackend.dto.NoteRequest;
import com.example.notesappbackend.dto.NoteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * PUBLIC_INTERFACE
 * Service interface for managing notes with CRUD operations,
 * partial updates, and paginated listing with optional filters.
 */
public interface NoteService {

    /**
     * PUBLIC_INTERFACE
     * Creates a new note from the provided request DTO.
     *
     * @param request note creation data
     * @return created note response DTO
     */
    NoteResponse create(NoteRequest request);

    /**
     * PUBLIC_INTERFACE
     * Retrieves a note by its identifier.
     *
     * @param id note identifier
     * @return note response DTO
     * @throws jakarta.persistence.EntityNotFoundException when the note is not found
     */
    NoteResponse getById(Long id);

    /**
     * PUBLIC_INTERFACE
     * Replaces a note's content using the provided request DTO.
     *
     * @param id      note identifier
     * @param request note data to replace with
     * @return updated note response DTO
     * @throws jakarta.persistence.EntityNotFoundException when the note is not found
     */
    NoteResponse update(Long id, NoteRequest request);

    /**
     * PUBLIC_INTERFACE
     * Partially updates a note. Only non-null fields in request are applied.
     *
     * @param id      note identifier
     * @param request request with fields to update (null fields are ignored)
     * @return updated note response DTO
     * @throws jakarta.persistence.EntityNotFoundException when the note is not found
     */
    NoteResponse partialUpdate(Long id, NoteRequest request);

    /**
     * PUBLIC_INTERFACE
     * Deletes a note by its identifier.
     *
     * @param id note identifier
     * @throws jakarta.persistence.EntityNotFoundException when the note is not found
     */
    void delete(Long id);

    /**
     * PUBLIC_INTERFACE
     * Lists notes with pagination and optional filters. If q is provided, performs
     * a case-insensitive search over title and content. If tag is provided, filters
     * by tag containment (case-insensitive). If both are provided, applies q first,
     * then narrows to those whose tags contain tag.
     *
     * @param q        optional query for title/content search
     * @param tag      optional tag filter (contains, case-insensitive)
     * @param pageable pagination and sorting parameters
     * @return page of NoteResponse
     */
    Page<NoteResponse> list(String q, String tag, Pageable pageable);
}
