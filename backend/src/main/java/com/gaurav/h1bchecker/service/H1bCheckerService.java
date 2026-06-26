package com.gaurav.h1bchecker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gaurav.h1bchecker.model.H1bResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class H1bCheckerService {

    private static final Logger log = LoggerFactory.getLogger(H1bCheckerService.class);

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.api.url}")
    private String apiUrl;

    @Value("${anthropic.model}")
    private String model;

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final String SYSTEM_PROMPT = """
            You are an H-1B sponsorship researcher. The user gives you a company name.
            Search h1bdata.info for that company (URL: https://h1bdata.info/index.php?em=COMPANY&year=All+Years)
            and any other relevant sources to determine if they sponsor H-1B visas.

            Respond ONLY with a valid JSON object — no markdown, no backticks, no explanation outside the JSON.
            Use exactly this shape:
            {
              "sponsors": true or false or null,
              "totalPetitions": integer or null,
              "recentYear": integer or null,
              "avgSalary": integer or null,
              "topRole": "string or null",
              "summary": "2-3 sentence plain English summary",
              "confidence": "high" or "medium" or "low"
            }

            sponsors = true if filings found, false if none found, null if uncertain.
            confidence = high if data found on h1bdata.info, medium if inferred from other sources, low if uncertain.
            """;

    public H1bResponse lookup(String company) {
        try {
            String requestBody = buildRequestBody(company);
            log.info("Calling Anthropic API for company: {}", company);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(60))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Anthropic API status: {}", response.statusCode());

            if (response.statusCode() != 200) {
                log.error("Anthropic API error body: {}", response.body());
                return H1bResponse.error(company, "Anthropic API returned status " + response.statusCode());
            }

            return parseResponse(company, response.body());

        } catch (Exception e) {
            log.error("Failed to call Anthropic API", e);
            return H1bResponse.error(company, "Request failed: " + e.getMessage());
        }
    }

    private String buildRequestBody(String company) throws Exception {
        ObjectNode body = mapper.createObjectNode();
        body.put("model", model);
        body.put("max_tokens", 1024);
        body.put("system", SYSTEM_PROMPT);

        // Add web search tool
        ArrayNode tools = mapper.createArrayNode();
        ObjectNode webSearch = mapper.createObjectNode();
        webSearch.put("type", "web_search_20250305");
        webSearch.put("name", "web_search");
        webSearch.put("max_uses", 3);
        tools.add(webSearch);
        body.set("tools", tools);

        // User message
        ArrayNode messages = mapper.createArrayNode();
        ObjectNode userMsg = mapper.createObjectNode();
        userMsg.put("role", "user");
        userMsg.put("content", "Check H-1B sponsorship history for: " + company);
        messages.add(userMsg);
        body.set("messages", messages);

        return mapper.writeValueAsString(body);
    }

    private H1bResponse parseResponse(String company, String responseBody) throws Exception {
        JsonNode root = mapper.readTree(responseBody);
        JsonNode content = root.path("content");

        // Find the final text block (Claude's JSON answer)
        String jsonText = null;
        for (JsonNode block : content) {
            if ("text".equals(block.path("type").asText())) {
                jsonText = block.path("text").asText();
            }
        }

        if (jsonText == null || jsonText.isBlank()) {
            log.error("No text block found in response: {}", responseBody);
            return H1bResponse.error(company, "No response text from model");
        }

        // Strip any accidental markdown fences
        jsonText = jsonText.replaceAll("(?s)```json\\s*", "").replaceAll("```", "").trim();
        log.debug("Parsed JSON text: {}", jsonText);

        JsonNode result = mapper.readTree(jsonText);

        H1bResponse resp = new H1bResponse();
        resp.setCompany(company);
        resp.setSponsors(result.hasNonNull("sponsors") ? result.path("sponsors").asBoolean() : null);
        resp.setTotalPetitions(result.hasNonNull("totalPetitions") ? result.path("totalPetitions").asInt() : null);
        resp.setRecentYear(result.hasNonNull("recentYear") ? result.path("recentYear").asInt() : null);
        resp.setAvgSalary(result.hasNonNull("avgSalary") ? result.path("avgSalary").asInt() : null);
        resp.setTopRole(result.hasNonNull("topRole") ? result.path("topRole").asText() : null);
        resp.setSummary(result.path("summary").asText());
        resp.setConfidence(result.path("confidence").asText());
        resp.setH1bDataUrl("https://h1bdata.info/index.php?em=" +
                company.replace(" ", "+") + "&year=All+Years");

        return resp;
    }
}
