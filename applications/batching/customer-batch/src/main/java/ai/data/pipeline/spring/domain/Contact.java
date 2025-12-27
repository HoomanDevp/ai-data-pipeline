package ai.data.pipeline.spring.domain;

import lombok.Builder;

@Builder
public record Contact(String email, String phone) {
}
