# Spring AI in Action — Chapter 1 & 2 (Board Game Buddy)

Praktikum UAS ADSE berdasarkan buku *Spring AI in Action* (Craig Walls).

| Folder | Isi |
|---|---|
| `chapter01/` | Getting started with Spring AI: `ChatClient`, REST `POST /ask`, test WireMock |
| `chapter02/` | Evaluating generated responses: `RelevancyEvaluator`, `FactCheckingEvaluator`, self-evaluation + Spring Retry |

## Teknologi
Java 23 · Spring Boot 3.5.5 · Spring AI 1.0.1 (`spring-ai-starter-model-openai`) · Gradle 8.14.3 · WireMock 3.9.0 · Spring Retry

## Menjalankan
```bash
cd chapter01        # atau chapter02
./gradlew test      # menjalankan unit/integration test
./gradlew bootRun   # buka http://localhost:8080
curl localhost:8080/ask -H "Content-type: application/json" -d '{"question":"Why is the sky blue?"}'
```

## Mode demo vs OpenAI asli
Secara default `ChatClient` diarahkan ke `MockAiController` (`/mock-ai/v1/chat/completions`),
API tiruan berformat OpenAI di dalam aplikasi sendiri, sehingga tidak perlu API key.
Untuk OpenAI asli:
```bash
export SPRING_AI_OPENAI_API_KEY=sk-...
./gradlew bootRun --args="--spring.profiles.active=openai"
```
