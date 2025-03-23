package org.example.easyocr.controller;

import org.example.easyocr.service.OcrService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/ocr")
public class OcrController {

    private static final Logger logger = Logger.getLogger(OcrController.class.getName());
    private final OcrService ocrService;

    public OcrController(OcrService ocrService) {
        this.ocrService = ocrService;
    }

    /*
     * Process an image file and return the extracted text
     * @param image The image file to process
     * @return A map containing the extracted text
     */
    @PostMapping(value = "/process", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> processImage(@RequestParam("image") MultipartFile image) {
        try {
            if (image == null || image.isEmpty()) {
                logger.warning("No image file provided");
                return ResponseEntity.badRequest().body(Map.of("error", "No image provided"));
            }
            
            // Log basic image info
            logger.info("Processing image: " + image.getOriginalFilename() + 
                        ", Size: " + image.getSize() + 
                        ", Content-Type: " + image.getContentType());
            
            // Validate image type
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                logger.warning("Invalid content type: " + contentType);
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid file type. Please upload an image"));
            }
            
            Map<String, Object> result = ocrService.processImage(image);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error processing image", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Error processing image: " + e.getMessage()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error", e);
            return ResponseEntity.badRequest().body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PostMapping(value = "/process-base64")
    public ResponseEntity<Map<String, Object>> processBase64Image(@RequestBody Map<String, String> request) {
        try {
            String base64Image = request.get("image");
            if (base64Image == null || base64Image.isEmpty()) {
                logger.warning("No base64 image data provided");
                return ResponseEntity.badRequest().body(Map.of("error", "No image data provided"));
            }
            
            // Log the size of the base64 string
            logger.info("Base64 image length: " + base64Image.length());
            
            // Check if it has a valid base64 prefix
            if (!base64Image.contains(";base64,") && !base64Image.startsWith("data:image")) {
                logger.warning("Invalid base64 format, missing prefix");
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid base64 format"));
            }
            
            Map<String, Object> result = ocrService.processBase64Image(base64Image);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error processing base64 image", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Error processing image: " + e.getMessage()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Process a document image (ID card, driver's license, etc.) and return structured JSON data
     * 
     * @param image The document image file to process
     * @return A JSON object containing the extracted fields
     */
    @PostMapping(value = "/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> processDocumentImage(@RequestParam("image") MultipartFile image) {
        try {
            if (image == null || image.isEmpty()) {
                logger.warning("No document image file provided");
                return ResponseEntity.badRequest().body(Map.of("error", "No document image provided"));
            }
            
            // Log basic image info
            logger.info("Processing document image: " + image.getOriginalFilename() + 
                        ", Size: " + image.getSize() + 
                        ", Content-Type: " + image.getContentType());
            
            // Validate image type
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                logger.warning("Invalid content type: " + contentType);
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid file type. Please upload an image"));
            }
            
            Map<String, Object> result = ocrService.processDocumentImage(image);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error processing document image", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Error processing document image: " + e.getMessage()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error", e);
            return ResponseEntity.badRequest().body(Map.of("error", "An unexpected error occurred: " + e.getMessage()));
        }
    }

    /**
     * Process a base64 encoded document image and return structured JSON data
     * 
     * @param request Map containing the base64 encoded image
     * @return A JSON object containing the extracted fields
     */
    @PostMapping(value = "/document-base64")
    public ResponseEntity<Map<String, Object>> processDocumentBase64Image(@RequestBody Map<String, String> request) {
        try {
            String base64Image = request.get("image");
            if (base64Image == null || base64Image.isEmpty()) {
                logger.warning("No base64 document image data provided");
                return ResponseEntity.badRequest().body(Map.of("error", "No document image data provided"));
            }
            
            // Log the size of the base64 string
            logger.info("Base64 document image length: " + base64Image.length());
            
            // Check if it has a valid base64 prefix
            if (!base64Image.contains(";base64,") && !base64Image.startsWith("data:image")) {
                logger.warning("Invalid base64 format, missing prefix");
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid base64 format"));
            }
            
            Map<String, Object> result = ocrService.processDocumentBase64Image(base64Image);
            return ResponseEntity.ok(result);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error processing base64 document image", e);
            return ResponseEntity.badRequest().body(Map.of("error", "Error processing document image: " + e.getMessage()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
} 