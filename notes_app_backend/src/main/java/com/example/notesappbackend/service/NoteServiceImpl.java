package com.example.notesappbackend.service;

import com.example.notesappbackend.dto.NoteRequest;
import com.example.notesappbackend.dto.NoteResponse;
import com.example.notesappbackend.model.Note;
import com.example.notesappbackend.repository.NoteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service implementation for the NoteService interface
 * providing business logic for CRUD operations, partial updates,
 * and paginated listings with optional query and tag filters.
 */
@Service
@Transactional
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;

    public NoteServiceImpl(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    // PUBLIC_INTERFACE
    @Override
    public NoteResponse create(NoteRequest request) {
        Note entity = new Note()
                .setTitle(request.getTitle())
                .setContent(request.getContent())
                .setTags(normalizeTags(request.getTags()));

        Note saved = noteRepository.save(entity);
        return toResponse(saved);
    }

    // PUBLIC_INTERFACE
    @Override
    @Transactional(readOnly = true)
    public NoteResponse getById(Long id) {
        Note note = findOrThrow(id);
        return toResponse(note);
    }

    // PUBLIC_INTERFACE
    @Override
    public NoteResponse update(Long id, NoteRequest request) {
        Note note = findOrThrow(id);

        // Replace all updatable fields
        note.setTitle(request.getTitle());
        note.setContent(request.getContent());
        note.setTags(normalizeTags(request.getTags()));

        Note saved = noteRepository.save(note);
        return toResponse(saved);
    }

    // PUBLIC_INTERFACE
    @Override
    public NoteResponse partialUpdate(Long id, NoteRequest request) {
        Note note = findOrThrow(id);

        // Only apply non-null fields
        if (request.getTitle() != null) {
            note.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            note.setContent(request.getContent());
        }
        if (request.getTags() != null) {
            note.setTags(normalizeTags(request.getTags()));
        }

        Note saved = noteRepository.save(note);
        return toResponse(saved);
    }

    // PUBLIC_INTERFACE
    @Override
    public void delete(Long id) {
        // Ensure existence, then delete
        Note note = findOrThrow(id);
        noteRepository.delete(note);
    }

    // PUBLIC_INTERFACE
    @Override
    @Transactional(readOnly = true)
    public Page<NoteResponse> list(String q, String tag, Pageable pageable) {
        Page<Note> page;

        boolean hasQ = q != null && !q.isBlank();
        boolean hasTag = tag != null && !tag.isBlank();

        if (hasQ && hasTag) {
            Page<Note> byQ = noteRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(q, q, pageable);
            String tagLower = tag.toLowerCase();
            List<Note> filteredContent = byQ.getContent().stream()
                    .filter(n -> containsIgnoreCase(n.getTags(), tagLower))
                    .collect(Collectors.toList());
            // Note: total elements reflect only the filtered items on this page for simplicity.
            // A fully correct total count would require a dedicated repository method or specification query.
            Page<Note> filteredPage = new PageImpl<>(filteredContent, pageable, filteredContent.size());
            return filteredPage.map(this::toResponse);
        } else if (hasQ) {
            page = noteRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(q, q, pageable);
        } else if (hasTag) {
            page = noteRepository.findByTagsContainingIgnoreCase(tag, pageable);
        } else {
            page = noteRepository.findAll(pageable);
        }

        return page.map(this::toResponse);
    }

    /**
     * Maps a Note entity to a NoteResponse DTO.
     *
     * @param note entity to map
     * @return DTO representation
     */
    private NoteResponse toResponse(Note note) {
        return new NoteResponse()
                .setId(note.getId())
                .setTitle(note.getTitle())
                .setContent(note.getContent())
                .setTags(note.getTags())
                .setCreatedAt(note.getCreatedAt())
                .setUpdatedAt(note.getUpdatedAt());
    }

    /**
     * Normalizes tags string (comma-separated) by trimming whitespace; returns null if blank.
     *
     * @param tags raw tags string
     * @return normalized tags or null
     */
    private String normalizeTags(String tags) {
        if (tags == null) {
            return null;
        }
        String trimmed = tags.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Loads a Note by id or throws EntityNotFoundException.
     *
     * @param id note id
     * @return Note entity
     */
    private Note findOrThrow(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Note not found with id " + id));
    }

    /**
     * Checks whether haystack contains needle ignoring case. Handles nulls gracefully.
     *
     * @param haystack text to search in
     * @param needle   text to search for
     * @return true if contains ignoring case
     */
    private boolean containsIgnoreCase(String haystack, String needle) {
        if (haystack == null || needle == null) {
            return false;
        }
        return haystack.toLowerCase().contains(needle.toLowerCase());
    }
}
