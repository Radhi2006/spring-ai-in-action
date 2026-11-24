package com.example.boardgamebuddy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

@Service
@Primary   // dipilih ketika BoardGameService di-inject (ada 2 implementasi)
public class SelfEvaluatingBoardGameService implements BoardGameService {

    private static final Logger log =
            LoggerFactory.getLogger(SelfEvaluatingBoardGameService.class);

    private final ChatClient chatClient;
    private final RelevancyEvaluator evaluator;

    public SelfEvaluatingBoardGameService(
            ChatClient.Builder chatClientBuilder,
            @Value("${spring.ai.openai.chat.options.model:gpt-4o-mini}") String model) {

        // Buku: .model("gpt-4o-mini"); di sini model dibaca dari konfigurasi
        // agar bisa berjalan di Ollama (llama3.2:3b) maupun OpenAI.
        var chatOptions = ChatOptions.builder()
                .model(model)
                .build();

        this.chatClient = chatClientBuilder
                .defaultOptions(chatOptions)
                .build();

        this.evaluator = new RelevancyEvaluator(chatClientBuilder);   // #1
    }

    @Override                                                         // #2
    @Retryable(retryFor = AnswerNotRelevantException.class)
    public Answer askQuestion(Question question) {
        var answerText = chatClient.prompt()
                .user(question.question())
                .call()
                .content();

        evaluateRelevancy(question, answerText);

        return new Answer(answerText);
    }

    @Recover                                                          // #3
    public Answer recover(AnswerNotRelevantException e) {
        log.warn("Semua percobaan gagal: {}", e.getMessage());
        return new Answer("I'm sorry, I wasn't able to answer the question.");
    }

    private void evaluateRelevancy(Question question, String answerText) {
        var evaluationRequest =
                new EvaluationRequest(question.question(), answerText);
        var evaluationResponse = evaluator.evaluate(evaluationRequest);
        log.info("Relevancy evaluation -> pass={}", evaluationResponse.isPass());

        if (!evaluationResponse.isPass()) {
            throw new AnswerNotRelevantException(question.question(), answerText); // #4
        }
    }

}
