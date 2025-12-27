package ai.data.pipeline.sentiment.processor;

import ai.data.pipeline.sentiment.domains.CustomerFeedback;
import ai.data.pipeline.sentiment.domains.FeedbackSentiment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomerFeedbackSentimentProcessor implements Function<CustomerFeedback, FeedbackSentiment> {
    private final ChatClient chatClient;

    private final String prompt = """
            Analyze the sentiment of this text: "{text}".
            Respond with only one word: Positive or Negative.
            """;

    @Override
    public FeedbackSentiment apply(CustomerFeedback customerFeedback) {

        log.info("customerFeedback: {}",customerFeedback);
        var sentiment = chatClient.prompt()
                .user(u -> u.text(prompt)
                        .param("text", customerFeedback.summary()))
                .call()
                .entity(FeedbackSentiment.Sentiment.class);

        log.info("sentiment: {}",sentiment);

        return FeedbackSentiment.builder()
                .customerFeedback(customerFeedback)
                .sentiment(sentiment).build();
    }
}
