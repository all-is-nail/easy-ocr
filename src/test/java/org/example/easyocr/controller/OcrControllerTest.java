package org.example.easyocr.controller;

import org.example.easyocr.service.OcrService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OcrControllerTest {

    @Mock
    private OcrService ocrService;

    @InjectMocks
    private OcrController ocrController;

    @Test
    public void testProcessDocumentImage() throws IOException {
        // Given
        MockMultipartFile mockFile = new MockMultipartFile(
                "image",
                "test-id-card.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );
        
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("document_type", "ID Card");
        expectedResult.put("document_number", "123456789");
        expectedResult.put("name", "John Doe");
        expectedResult.put("date_of_birth", "1990-01-01");
        
        when(ocrService.processDocumentImage(any(MultipartFile.class))).thenReturn(expectedResult);
        
        // When
        ResponseEntity<Map<String, Object>> response = ocrController.processDocumentImage(mockFile);
        
        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("ID Card", responseBody.get("document_type"));
        assertEquals("123456789", responseBody.get("document_number"));
        assertEquals("John Doe", responseBody.get("name"));
        assertEquals("1990-01-01", responseBody.get("date_of_birth"));
    }
    
    @Test
    public void testProcessDocumentBase64Image() throws IOException {
        // Given
        String base64Image = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQEAYABgAAD/2wBDAAIBAQIBAQICAgICAgICAwUDAwMDAwYEBAMFBwYHBwcGBwcICQsJCAgKCAcHCg0KCgsMDAwMBwkODw0MDgsMDAz/2wBDAQICAgMDAwYDAwYMCAcIDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAz/wAARCAABAAEDASIAAhEBAxEB/8QAHwAAAQUBAQEBAQEAAAAAAAAAAAECAwQFBgcICQoL/8QAtRAAAgEDAwIEAwUFBAQAAAF9AQIDAAQRBRIhMUEGE1FhByJxFDKBkaEII0KxwRVS0fAkM2JyggkKFhcYGRolJicoKSo0NTY3ODk6Q0RFRkdISUpTVFVWV1hZWmNkZWZnaGlqc3R1dnd4eXqDhIWGh4iJipKTlJWWl5iZmqKjpKWmp6ipqrKztLW2t7i5usLDxMXGx8jJytLT1NXW19jZ2uHi4+Tl5ufo6erx8vP09fb3+Pn6/8QAHwEAAwEBAQEBAQEBAQAAAAAAAAECAwQFBgcICQoL/8QAtREAAgECBAQDBAcFBAQAAQJ3AAECAxEEBSExBhJBUQdhcRMiMoEIFEKRobHBCSMzUvAVYnLRChYkNOEl8RcYGRomJygpKjU2Nzg5OkNERUZHSElKU1RVVldYWVpjZGVmZ2hpanN0dXZ3eHl6goOEhYaHiImKkpOUlZaXmJmaoqOkpaanqKmqsrO0tba3uLm6wsPExcbHyMnK0tPU1dbX2Nna4uPk5ebn6Onq8vP09fb3+Pn6/9oADAMBAAIRAxEAPwD9/KKKKAP/2Q==";
        Map<String, String> request = new HashMap<>();
        request.put("image", base64Image);
        
        Map<String, Object> expectedResult = new HashMap<>();
        expectedResult.put("document_type", "Driver's License");
        expectedResult.put("document_number", "DL123456");
        expectedResult.put("name", "Jane Smith");
        expectedResult.put("expiry_date", "2025-05-15");
        
        when(ocrService.processDocumentBase64Image(any(String.class))).thenReturn(expectedResult);
        
        // When
        ResponseEntity<Map<String, Object>> response = ocrController.processDocumentBase64Image(request);
        
        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals("Driver's License", responseBody.get("document_type"));
        assertEquals("DL123456", responseBody.get("document_number"));
        assertEquals("Jane Smith", responseBody.get("name"));
        assertEquals("2025-05-15", responseBody.get("expiry_date"));
    }
} 