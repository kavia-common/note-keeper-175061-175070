# Notes App Backend API

## Overview
This backend provides a RESTful API for creating, viewing, editing, deleting, and listing notes. It is built with Spring Boot and uses an in-memory H2 database by default for development. OpenAPI/Swagger UI is available for interactive documentation.

- Base URL: http://localhost:3001
- Health check: GET /health → OK
- Root: GET / → Redirects to /docs (Swagger UI)

## Quick Start
- Run the Spring Boot app (Gradle): ./gradlew bootRun
- Swagger UI:
  - Redirect helper: GET /docs (redirects to /swagger-ui.html preserving scheme/host/port)
  - Direct: GET /swagger-ui.html
  - OpenAPI JSON: GET /api-docs
- H2 Console: GET /h2-console (JDBC URL: jdbc:h2:mem:testdb, user: sa, password: empty)

Configuration references:
- Swagger/OpenAPI paths set in application.properties:
  - springdoc.api-docs.path=/api-docs
  - springdoc.swagger-ui.path=/swagger-ui.html
- H2 console:
  - spring.h2.console.enabled=true

## Resources
- Notes collection: /api/notes
- Single note: /api/notes/{id}

## Endpoints

### Create a note
- Method: POST
- URL: /api/notes
- Body (application/json):
{
  "title": "My note title",
  "content": "Optional note content",
  "tags": "comma,separated,tags"
}
- Success: 201 Created
  - Location header: /api/notes/{id}
  - Response: NoteResponse
- Validations:
  - title: required, max 200 chars
  - content: optional, max 10000 chars
  - tags: optional string, comma-separated

Example cURL:
curl -i -X POST "http://localhost:3001/api/notes" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Welcome to Notes",
    "content": "This is my first note",
    "tags": "welcome,personal"
  }'

### Get a note by id
- Method: GET
- URL: /api/notes/{id}
- Success: 200 OK
- Errors:
  - 404 Not Found if missing

Example cURL:
curl -i "http://localhost:3001/api/notes/1"

### Update a note (replace)
- Method: PUT
- URL: /api/notes/{id}
- Body: NoteRequest (same as POST)
- Success: 200 OK
- Errors: 400 validation, 404 not found

Example cURL:
curl -i -X PUT "http://localhost:3001/api/notes/1" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Updated title",
    "content": "Updated content",
    "tags": "updated,example"
  }'

### Partially update a note
- Method: PATCH
- URL: /api/notes/{id}
- Body: Partial NoteRequest (only non-null fields applied)
- Success: 200 OK
- Errors: 400 validation, 404 not found

Example cURL:
curl -i -X PATCH "http://localhost:3001/api/notes/1" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Partial update: title only"
  }'

### Delete a note
- Method: DELETE
- URL: /api/notes/{id}
- Success: 204 No Content
- Errors: 404 Not Found

Example cURL:
curl -i -X DELETE "http://localhost:3001/api/notes/1"

### List notes with pagination and filters
- Method: GET
- URL: /api/notes
- Query parameters:
  - page: integer, 0-based index, default 0, must be >= 0
  - size: integer, default 20, must be >= 1, capped at 200
  - sort: string, e.g., "createdAt,desc" or "title,asc"; default is "updatedAt,desc"
  - q: string, optional; case-insensitive search in title OR content
  - tag: string, optional; case-insensitive substring match within the comma-separated tags field
- Behavior when both q and tag present:
  - Performs q search first, then narrows in-memory to items whose tags contain tag (case-insensitive). The response totalElements reflects the filtered items on that page.

Example cURL (basic):
curl -i "http://localhost:3001/api/notes"

Example cURL (page/size/sort):
curl -i "http://localhost:3001/api/notes?page=0&size=10&sort=title,asc"

Example cURL (search text q):
curl -i "http://localhost:3001/api/notes?q=welcome"

Example cURL (tag filter):
curl -i "http://localhost:3001/api/notes?tag=personal"

Example cURL (combined q and tag):
curl -i "http://localhost:3001/api/notes?q=notes&tag=welcome&size=5"

## Data models

### NoteRequest
- title: string (required, max 200)
- content: string (optional, max 10000)
- tags: string (optional, comma-separated)

### NoteResponse
- id: number
- title: string
- content: string
- tags: string | null
- createdAt: string (ISO-8601 instant)
- updatedAt: string (ISO-8601 instant)

## Pagination response format
The list endpoint returns a Spring Page payload serialized as JSON. For documentation purposes NoteController includes a schema wrapper PageNoteResponseSchema. A typical response includes:

{
  "content": [
    {
      "id": 1,
      "title": "Welcome to Notes",
      "content": "This is your first note...",
      "tags": "welcome,getting-started",
      "createdAt": "2025-11-21T09:51:00.327Z",
      "updatedAt": "2025-11-21T09:51:00.327Z"
    }
  ],
  "number": 0,
  "size": 20,
  "totalElements": 3,
  "totalPages": 1,
  "first": true,
  "last": true,
  "sort": {
    "empty": false,
    "sorted": true,
    "unsorted": false
  },
  "numberOfElements": 3,
  "empty": false
}

Note: Depending on Jackson configuration, extra Page fields may appear (first, last, sort, numberOfElements, empty). The core fields to rely on are content, number, size, totalElements, totalPages.

## Standardized error payloads
All controllers under package com.example.notesappbackend are handled by GlobalExceptionHandler. Errors follow a consistent JSON structure:

{
  "timestamp": "2025-11-21T10:10:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/notes",
  "fieldErrors": {
    "title": "Title is required"
  }
}

Common cases:
- 404 Not Found (NotFoundException or EntityNotFoundException translated): 
{
  "timestamp": "...",
  "status": 404,
  "error": "Not Found",
  "message": "Note not found with id 123",
  "path": "/api/notes/123"
}
- 400 Bad Request (bean validation on @Valid in request body): includes fieldErrors map of field-to-message.
- 400 Bad Request (constraint violations on query/path params): includes fieldErrors with keys from the constraint path.
- 400 Bad Request (malformed JSON): message includes parsing error from HttpMessageNotReadableException.
- Other exceptions: 500 Internal Server Error with message "An unexpected error occurred" unless Spring provides a more specific status via ErrorResponseException.

## Swagger UI and API docs
- Redirect helper: GET /docs redirects to /swagger-ui.html with correct scheme/host/port.
- Swagger UI: GET /swagger-ui.html
- OpenAPI spec: GET /api-docs

These are configured in:
- application.properties
- HelloController.docs() for redirect behavior

## H2 Console
- URL: /h2-console
- JDBC URL: jdbc:h2:mem:testdb
- Username: sa
- Password: (empty)
- Enabled in application.properties: spring.h2.console.enabled=true

Note: H2 console is intended for local/dev. Disable in production.

## Sorting fields
The list endpoint accepts sort of the form field,direction. Valid directions are asc or desc. Default sort when unspecified is updatedAt,desc. Supported sortable fields include:
- updatedAt (default)
- createdAt
- title
If an invalid direction is provided, the service falls back to ascending for that field.

## HTTP examples

Create:
curl -i -X POST "http://localhost:3001/api/notes" \
  -H "Content-Type: application/json" \
  -d '{"title":"New","content":"Body","tags":"t1,t2"}'

Get:
curl -i "http://localhost:3001/api/notes/42"

Update:
curl -i -X PUT "http://localhost:3001/api/notes/42" \
  -H "Content-Type: application/json" \
  -d '{"title":"Updated","content":"Updated body","tags":"t1"}'

Patch:
curl -i -X PATCH "http://localhost:3001/api/notes/42" \
  -H "Content-Type: application/json" \
  -d '{"content":"Only content changed"}'

Delete:
curl -i -X DELETE "http://localhost:3001/api/notes/42"

List (filters and pagination):
curl -i "http://localhost:3001/api/notes?page=1&size=5&sort=createdAt,desc&q=guide&tag=welcome"

## Development notes
- Port is fixed at 3001 by configuration for previews and local dev.
- Sample data is seeded at startup in non-prod profiles (DataSeeder) if the database is empty.
- Actuator exposes health/info/metrics minimally by default.

## Source references
- Controllers: HelloController, NoteController
- DTOs: NoteRequest, NoteResponse
- Entity: Note
- Repository: NoteRepository
- Service: NoteService, NoteServiceImpl
- Errors: ApiError, GlobalExceptionHandler
- Configuration: src/main/resources/application.properties
