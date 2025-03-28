package org.example.easyocr.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OcrService {

    private static final Logger logger = Logger.getLogger(OcrService.class.getName());
    private final RestTemplate restTemplate;
    
    @Value("${openai.api.url}")
    private String apiUrl;
    
    @Value("${openai.api.key}")
    private String apiKey;
    
    @Value("${openai.model}")
    private String model;
    
    @Value("${openai.prompt}")
    private String defaultPrompt;
    
    @Value("${openai.structured.prompt}")
    private String structuredPrompt;

    public OcrService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> processImage(MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            logger.warning("Received empty or null image file");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "No image file provided or file is empty");
            return errorResponse;
        }
        
        logger.info("Processing image: " + image.getOriginalFilename() + ", size: " + image.getSize() + " bytes, contentType: " + image.getContentType());
        byte[] imageBytes = image.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        return processBase64Image(base64Image, image.getContentType());
    }

    /**
     * Process a document image for structured field extraction (ID cards, licenses, etc.)
     * @param image The document image to process
     * @return A map containing the extracted structured data
     */
    public Map<String, Object> processDocumentImage(MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            logger.warning("Received empty or null document image file");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "No document image file provided or file is empty");
            return errorResponse;
        }
        
        logger.info("Processing document image: " + image.getOriginalFilename() + ", size: " + image.getSize() + " bytes, contentType: " + image.getContentType());
        byte[] imageBytes = image.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(imageBytes);
        return processDocumentBase64Image(base64Image, image.getContentType());
    }

    public Map<String, Object> processBase64Image(String base64Image) throws IOException {
        return processBase64Image(base64Image, null);
    }

    public Map<String, Object> processDocumentBase64Image(String base64Image) throws IOException {
        return processDocumentBase64Image(base64Image, null);
    }

    public Map<String, Object> processDocumentBase64Image(String base64Image, String contentType) throws IOException {
        return processImageWithPrompt(base64Image, contentType, structuredPrompt);
    }

    public Map<String, Object> processBase64Image(String base64Image, String contentType) throws IOException {
        return processImageWithPrompt(base64Image, contentType, defaultPrompt);
    }

    private Map<String, Object> processImageWithPrompt(String base64Image, String contentType, String promptToUse) throws IOException {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            logger.warning("Received empty or null base64 image string");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "No image data provided");
            return errorResponse;
        }
        
        // Ensure the base64 string is properly formatted for the API
        if (base64Image.contains(",")) {
            String[] parts = base64Image.split(",");
            base64Image = parts[1];
            // Try to extract content type if not provided
            if (contentType == null && parts[0].contains(":") && parts[0].contains(";")) {
                contentType = parts[0].substring(parts[0].indexOf(":") + 1, parts[0].indexOf(";"));
            }
        }
        
        // Set default content type if not provided
        if (contentType == null) {
            contentType = "image/jpeg";
        }
        
        // Check if the base64 string is valid
        try {
            Base64.getDecoder().decode(base64Image);
        } catch (IllegalArgumentException e) {
            logger.warning("Invalid base64 string: " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid image data format");
            return errorResponse;
        }
        
        logger.info("Processing base64 image, length: " + base64Image.length() + ", contentType: " + contentType);
        logger.info("Using API URL: " + apiUrl);
        logger.info("Using model: " + model);
        logger.info("Using prompt: " + promptToUse);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("User-Agent", "EasyOCR/1.0");
        
        // Create the request payload according to OpenAI's documentation
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        
        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        
        List<Map<String, Object>> contentItems = new ArrayList<>();
        
        // Text part - Put a clear, direct instruction for structured extraction
        Map<String, Object> textItem = new HashMap<>();
        textItem.put("type", "text");
        textItem.put("text", promptToUse);
        
        // Image part
        Map<String, Object> imageItem = new HashMap<>();
        imageItem.put("type", "image_url");
        
        Map<String, String> imageUrl = new HashMap<>();
        // Use the correct content type in the data URI
        String dataUri = "data:" + contentType + ";base64," + base64Image;
        imageUrl.put("url", dataUri);
        imageItem.put("image_url", imageUrl);
        
        // Add text first, then image to the content
        contentItems.add(textItem);
        contentItems.add(imageItem);
        
        userMessage.put("content", contentItems);
        messages.add(userMessage);
        
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 4000);
        
        // Set parameters to get more direct OCR results
        requestBody.put("temperature", 0.1);
        requestBody.put("top_p", 1.0);
        requestBody.put("frequency_penalty", 0.0);
        requestBody.put("presence_penalty", 0.0);
        
        logger.info("Request body prepared with image data URI length: " + dataUri.length());
        
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        
        try {
            logger.info("Sending request to OpenAI API");
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(apiUrl, requestEntity, Map.class);
            logger.info("Response received with status: " + responseEntity.getStatusCode());
            return formatResponse(responseEntity.getBody());
        } catch (RestClientException e) {
            logger.log(Level.SEVERE, "Error calling OpenAI API", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to connect to the OpenAI API: " + e.getMessage());
            return errorResponse;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return errorResponse;
        }
    }
    
    private Map<String, Object> formatResponse(Map response) {
        Map<String, Object> result = new LinkedHashMap<>();
        
        if (response == null) {
            result.put("error", "No response received from API");
            return result;
        }
        
        if (response.containsKey("error")) {
            result.put("error", response.get("error"));
            return result;
        }
        
        try {
            logger.info("Parsing API response");
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> choice = choices.get(0);
                Map<String, Object> message = (Map<String, Object>) choice.get("message");
                String content = (String) message.get("content");
                
                // Try to parse the content as JSON
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    if (content.contains("{") && content.contains("}")) {
                        // Extract JSON portion if it's embedded in text
                        int startIdx = content.indexOf('{');
                        int endIdx = content.lastIndexOf('}') + 1;
                        if (startIdx >= 0 && endIdx > startIdx) {
                            String jsonPart = content.substring(startIdx, endIdx);
                            Map<String, Object> jsonData = mapper.readValue(jsonPart, Map.class);
                            
                            // Return the structured data directly
                            logger.info("Successfully parsed structured document data as JSON");
                            return jsonData;
                        }
                    }
                    
                    // If we couldn't extract JSON with simple substring, try to parse the whole response
                    try {
                        Map<String, Object> jsonData = mapper.readValue(content, Map.class);
                        logger.info("Successfully parsed full document data as JSON");
                        return jsonData;
                    } catch (Exception e) {
                        logger.warning("Could not parse response as JSON, returning as raw text: " + e.getMessage());
                    }
                } catch (Exception e) {
                    logger.warning("Failed to extract JSON from response: " + e.getMessage());
                }
                
                // Fallback to raw text if JSON parsing fails
                result.put("extracted_text", content);
                logger.info("Text extracted as raw content");
                
                return result;
            } else {
                logger.warning("No choices found in the API response");
                result.put("error", "No text could be extracted from the image");
                result.put("original_response", response);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error parsing API response", e);
            result.put("error", "Error parsing API response: " + e.getMessage());
            result.put("original_response", response);
        }
        
        return result;
    }
} 