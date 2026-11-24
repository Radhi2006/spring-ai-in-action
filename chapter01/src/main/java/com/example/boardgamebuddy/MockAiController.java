package com.example.boardgamebuddy;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * API AI tiruan (dummy) yang meniru endpoint OpenAI POST /v1/chat/completions.
 * Dipakai agar aplikasi bisa didemokan tanpa API key. Spring AI (ChatClient)
 * tidak tahu bedanya: ia tetap mengirim request format OpenAI ke sini.
 */
@RestController
@Profile("!openai")
public class MockAiController {

    @PostMapping("/mock-ai/v1/chat/completions")
    @SuppressWarnings("unchecked")
    public Map<String, Object> chatCompletions(@RequestBody Map<String, Object> request) {
        var messages = (List<Map<String, Object>>) request.get("messages");
        var prompt = String.valueOf(messages.get(messages.size() - 1).get("content"));
        var content = generate(prompt);

        return Map.of(
                "id", "chatcmpl-mock-" + System.nanoTime(),
                "object", "chat.completion",
                "created", System.currentTimeMillis() / 1000,
                "model", String.valueOf(request.getOrDefault("model", "mock-model")),
                "choices", List.of(Map.of(
                        "index", 0,
                        "message", Map.of("role", "assistant", "content", content),
                        "finish_reason", "stop")),
                "usage", Map.of("prompt_tokens", prompt.length() / 4,
                        "completion_tokens", content.length() / 4,
                        "total_tokens", (prompt.length() + content.length()) / 4));
    }

    /** "Otak" dummy: jawaban berdasarkan kata kunci pada prompt. */
    String generate(String prompt) {
        String p = prompt.toLowerCase();

        // Prompt dari RelevancyEvaluator ("Either YES or NO")
        if (p.contains("either yes or no")) {
            return p.contains("moon is approximately") ? "NO" : "YES";
        }
        // Prompt dari FactCheckingEvaluator ("Respond with \"yes\" ...")
        if (p.contains("respond with \"yes\"")) {
            return (p.contains("blueberry jam") || p.contains("moon is approximately")) ? "no" : "yes";
        }

        if (p.contains("sky") && p.contains("blue")) {
            return "The sky appears blue because of Rayleigh scattering. Sunlight is made of many "
                    + "colors; the shorter blue wavelengths are scattered by gas molecules in the "
                    + "atmosphere much more than the longer red wavelengths, so blue light reaches "
                    + "our eyes from every direction of the sky.";
        }
        if (p.contains("capital of france")) {
            return "Paris";
        }
        if (p.contains("chess")) {
            return "Chess is played by two players, White and Black. Each player starts with 16 pieces "
                    + "and the goal is to checkmate the opponent's king.";
        }
        if (p.contains("monopoly")) {
            return "The objective of Monopoly is to become the wealthiest player by buying, renting and "
                    + "trading properties, driving the other players into bankruptcy.";
        }
        if (p.contains("uno")) {
            return "UNO can be played by 2 to 10 players. The goal is to be the first to get rid of all "
                    + "your cards; say \"UNO\" when you have one card left.";
        }
        if (p.contains("catan")) {
            return "Catan is played by 3-4 players (5-6 with an expansion). Players collect resources, "
                    + "build roads and settlements, and the first to reach 10 victory points wins.";
        }
        if (p.contains("retry") || p.contains("irrelevant")) {
            // jawaban sengaja tidak relevan untuk demo self-evaluation (bab 2.3)
            return "The moon is approximately 239,900 miles from Earth.";
        }
        return "Board Game Buddy (demo mode) received your question: \"" + prompt.trim()
                + "\". Try asking about Chess, Monopoly, UNO, Catan, or why the sky is blue.";
    }
}
