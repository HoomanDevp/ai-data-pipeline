package ai.data.pipeline.postgres.embedding.domain;

import lombok.Builder;

@Builder
public record SimilarDocuments(String id, String similaritiesPayload) {
}