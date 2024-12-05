package org.example;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;

public class DocumentManager {

    private final Map<String, Document> storage = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getId() == null && document.getId().isEmpty()) {
            document.setId(UUID.randomUUID().toString());
        }

        Document existingDocument = storage.get(document.getId());
        if (existingDocument != null) {
            document.setCreated(existingDocument.getCreated());
        } else {
            document.setCreated(Instant.now());
        }

        storage.put(document.getId(), document);
        return document;
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {

        List<Document> allDocuments = storage.values().stream().toList();
        return allDocuments
                .stream()
                .filter(doc -> matchesRequest(doc, request))
                .toList();
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    private boolean matchesRequest(Document document, SearchRequest request) {
        if (request == null) {
            return true;
        }

        boolean matchesTitle = request.getTitlePrefixes() == null ||
                request.getTitlePrefixes().stream().anyMatch(prefix -> document.getTitle().startsWith(prefix));

        boolean matchesContents = request.getContainsContents() == null ||
                request.getContainsContents().stream().anyMatch(content -> document.getContent().contains(content));

        boolean matchesAuthor = request.getAuthorIds() == null ||
                (document.getAuthor() != null && request.getAuthorIds().contains(document.getAuthor().getId()));

        boolean matchesCreatedFrom = request.getCreatedFrom() == null ||
                (document.getCreated() != null && !document.getCreated().isBefore(request.getCreatedFrom()));

        boolean matchesCreatedTo = request.getCreatedFrom() == null ||
                (document.getCreated() != null && !document.getCreated().isAfter(request.getCreatedFrom()));

        return matchesTitle && matchesContents && matchesAuthor && matchesCreatedFrom && matchesCreatedTo;
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
