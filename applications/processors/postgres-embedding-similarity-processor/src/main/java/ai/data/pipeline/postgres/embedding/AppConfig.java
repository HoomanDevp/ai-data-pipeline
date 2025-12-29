package ai.data.pipeline.postgres.embedding;

import ai.data.pipeline.postgres.embedding.properties.EmbeddingSimilarityProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EmbeddingSimilarityProperties.class)
public class AppConfig {
}
