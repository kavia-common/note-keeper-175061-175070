package com.example.notesappbackend;

import com.example.notesappbackend.dto.NoteRequest;
import com.example.notesappbackend.dto.NoteResponse;
import com.example.notesappbackend.service.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * REST controller exposing CRUD operations for notes, with pagination and optional filters.
 * Endpoints:
 * - POST   /api/notes
 * - GET    /api/notes/{id}
 * - PUT    /api/notes/{id}
 * - PATCH  /api/notes/{id}
 * - DELETE /api/notes/{id}
 * - GET    /api/notes
 *
 * Lightweight responses and minimal logging to ensure fast startup and avoid timeouts.
 */
@RestController
@RequestMapping("/api/notes")
@Validated
@CrossOrigin // basic CORS to prevent frontend issues
@Tag(name = "Notes", description = "CRUD operations for notes with pagination and filters")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * PUBLIC_INTERFACE
     * Creates a new note.
     *
     * @param request NoteRequest payload (validated)
     * @return 201 Created with Location header and NoteResponse body
     */
    @PostMapping
    @Operation(
            summary = "Create a note",
            description = "Creates a new note and returns the created resource.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Created",
                            content = @Content(schema = @Schema(implementation = NoteResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error")
            }
    )
    public ResponseEntity<NoteResponse> create(
            @Valid @RequestBody NoteRequest request
    ) {
        NoteResponse created = noteService.create(request);

        // Build Location header for created resource
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location).body(created);
    }

    /**
     * PUBLIC_INTERFACE
     * Retrieves a note by id.
     *
     * @param id note id
     * @return NoteResponse
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Get a note",
            description = "Retrieves a note by its identifier.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK",
                            content = @Content(schema = @Schema(implementation = NoteResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Not found")
            }
    )
    public ResponseEntity<NoteResponse> getById(
            @PathVariable("id") Long id
    ) {
        NoteResponse note = noteService.getById(id);
        return ResponseEntity.ok(note);
    }

    /**
     * PUBLIC_INTERFACE
     * Replaces a note's content.
     *
     * @param id      note id
     * @param request payload to replace with
     * @return updated NoteResponse
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Update a note",
        description = "Replaces a note with the provided payload.",
        responses = {
                @ApiResponse(responseCode = "200", description = "OK",
                        content = @Content(schema = @Schema(implementation = NoteResponse.class))),
                @ApiResponse(responseCode = "400", description = "Validation error"),
                @ApiResponse(responseCode = "404", description = "Not found")
        }
    )
    public ResponseEntity<NoteResponse> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody NoteRequest request
    ) {
        NoteResponse updated = noteService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * PUBLIC_INTERFACE
     * Partially updates a note. Only non-null fields are applied.
     *
     * @param id      note id
     * @param request partial payload
     * @return updated NoteResponse
     */
    @PatchMapping("/{id}")
    @Operation(
        summary = "Partially update a note",
        description = "Applies partial changes to a note. Only provided fields are updated.",
        responses = {
                @ApiResponse(responseCode = "200", description = "OK",
                        content = @Content(schema = @Schema(implementation = NoteResponse.class))),
                @ApiResponse(responseCode = "400", description = "Validation error"),
                @ApiResponse(responseCode = "404", description = "Not found")
        }
    )
    public ResponseEntity<NoteResponse> partialUpdate(
            @PathVariable("id") Long id,
            @Valid @RequestBody NoteRequest request
    ) {
        NoteResponse updated = noteService.partialUpdate(id, request);
        return ResponseEntity.ok(updated);
    }

    /**
     * PUBLIC_INTERFACE
     * Deletes a note by id.
     *
     * @param id note id
     * @return 204 No Content
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a note",
        description = "Deletes a note by its identifier.",
        responses = {
                @ApiResponse(responseCode = "204", description = "No Content"),
                @ApiResponse(responseCode = "404", description = "Not found")
        }
    )
    public ResponseEntity<Void> delete(
            @PathVariable("id") Long id
    ) {
        noteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUBLIC_INTERFACE
     * Lists notes with pagination and optional filters q (search title/content) and tag (contains, case-insensitive).
     *
     * @param page  page number (0-based)
     * @param size  page size (1..200)
     * @param sort  optional sort parameter, e.g., "createdAt,desc" or "title,asc"
     * @param q     optional search in title or content (case-insensitive)
     * @param tag   optional tag filter (contains, case-insensitive)
     * @return page of NoteResponse
     */
    @GetMapping
    @Operation(
        summary = "List notes",
        description = "Returns a paginated list of notes with optional query and tag filters.",
        responses = {
                @ApiResponse(responseCode = "200", description = "OK",
                        content = @Content(schema = @Schema(implementation = PageNoteResponseSchema.class)))
        }
    )
    public ResponseEntity<Page<NoteResponse>> list(
            @RequestParam(name = "page", defaultValue = "0")
            @Parameter(description = "0-based page index")
            @Min(value = 0, message = "page must be >= 0") int page,

            @RequestParam(name = "size", defaultValue = "20")
            @Parameter(description = "Page size (1..200)")
            @Min(value = 1, message = "size must be >= 1") int size,

            @RequestParam(name = "sort", required = false)
            @Parameter(description = "Sort by field, e.g., 'createdAt,desc'") String sort,

            @RequestParam(name = "q", required = false)
            @Parameter(description = "Query string to search in title/content (case-insensitive)") String q,

            @RequestParam(name = "tag", required = false)
            @Parameter(description = "Tag filter (contains, case-insensitive)") String tag
    ) {
        // Cap size to a reasonable maximum to keep responses lightweight
        int pageSize = Math.min(size, 200);

        Pageable pageable = buildPageable(page, pageSize, sort);
        Page<NoteResponse> result = noteService.list(q, tag, pageable);

        // Lightweight: avoid extra headers, just return the page as JSON
        return new ResponseEntity<>(result, new HttpHeaders(), HttpStatus.OK);
    }

    private Pageable buildPageable(int page, int size, String sort) {
        if (sort == null || sort.isBlank()) {
            // default sort by updatedAt desc for a sensible ordering
            return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
        }
        // parse "field,direction" format; fallback to ascending if direction missing/invalid
        String[] parts = sort.split(",", 2);
        String property = parts[0].trim();
        Sort.Direction direction = Sort.Direction.ASC;
        if (parts.length > 1) {
            try {
                direction = Sort.Direction.fromString(parts[1].trim());
            } catch (IllegalArgumentException ignored) {
                // keep default ASC to avoid throwing and slowing startup
            }
        }
        return PageRequest.of(page, size, Sort.by(direction, property));
    }

    /**
     * Wrapper schema used for documenting Page<NoteResponse> in OpenAPI.
     * Helps OpenAPI render a sensible schema for the paginated payload.
     */
    static class PageNoteResponseSchema {
        @Schema(description = "Page content")
        public NoteResponse[] content;

        @Schema(description = "Current page number (0-based)")
        public int number;

        @Schema(description = "Page size")
        public int size;

        @Schema(description = "Total elements across all pages")
        public long totalElements;

        @Schema(description = "Total number of pages")
        public int totalPages;
    }
}
