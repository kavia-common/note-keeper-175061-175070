package com.example.notesappbackend.setup;

import com.example.notesappbackend.model.Note;
import com.example.notesappbackend.repository.NoteRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds sample data on application startup in non-production profiles.
 * This runner is active only when the 'prod' profile is NOT active.
 */
@Component
@Profile("!prod")
public class DataSeeder implements CommandLineRunner {

    private final NoteRepository noteRepository;

    /**
     * PUBLIC_INTERFACE
     * Constructs the DataSeeder with the required repository.
     *
     * @param noteRepository repository for persisting sample notes
     */
    public DataSeeder(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    /**
     * PUBLIC_INTERFACE
     * Runs on application startup to insert a few sample notes if the repository is empty.
     *
     * @param args application arguments (unused)
     */
    @Override
    public void run(String... args) {
        // Avoid re-seeding if there is already data
        if (noteRepository.count() > 0) {
            return;
        }

        // Insert a few lightweight sample notes for local/dev/test profiles
        List<Note> samples = List.of(
                new Note("Welcome to Notes", "This is your first note. Feel free to edit or delete it.", "welcome,getting-started"),
                new Note("Using Tags", "Add comma-separated tags to organize your notes by topics.", "tips,organization"),
                new Note("Search and Pagination", "Use the list endpoint with q and tag filters to discover notes.", "api,features")
        );

        noteRepository.saveAll(samples);
    }
}
