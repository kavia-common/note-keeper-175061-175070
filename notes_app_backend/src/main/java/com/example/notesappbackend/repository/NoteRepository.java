package com.example.notesappbackend.repository;

import com.example.notesappbackend.model.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Note entity providing CRUD and custom query methods.
 * Includes case-insensitive search on title/content and tag filtering with pagination support.
 */
// PUBLIC_INTERFACE
public interface NoteRepository extends JpaRepository<Note, Long> {

    /**
     * Searches notes whose title or content contains the given text (case-insensitive).
     *
     * @param title    the title fragment to search (ignored case)
     * @param content  the content fragment to search (ignored case)
     * @param pageable pagination and sorting information
     * @return a page of notes matching the criteria
     */
    // PUBLIC_INTERFACE
    Page<Note> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(String title, String content, Pageable pageable);

    /**
     * Filters notes whose tags string contains the provided tag (case-insensitive).
     * Note: tags are stored as a comma-separated string in the Note entity.
     *
     * @param tag      the tag text to search inside the tags field (ignored case)
     * @param pageable pagination and sorting information
     * @return a page of notes matching the tag criterion
     */
    // PUBLIC_INTERFACE
    Page<Note> findByTagsContainingIgnoreCase(String tag, Pageable pageable);
}
