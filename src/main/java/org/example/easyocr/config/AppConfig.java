package org.example.easyocr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
public class AppConfig implements WebMvcConfigurer {

    private static final Logger logger = Logger.getLogger(AppConfig.class.getName());

    @Bean
    public RestTemplate restTemplate() {
        // Create a factory with request/response logging
        ClientHttpRequestFactory factory = new BufferingClientHttpRequestFactory(clientHttpRequestFactory());
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Add request/response logging interceptor
        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
        if (CollectionUtils.isEmpty(interceptors)) {
            interceptors = new ArrayList<>();
        }
        interceptors.add((request, body, execution) -> {
            logger.info("Making request to: " + request.getURI());
            logger.info("Request method: " + request.getMethod());
            try {
                return execution.execute(request, body);
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error during API request", e);
                throw e;
            }
        });
        restTemplate.setInterceptors(interceptors);
        
        return restTemplate;
    }
    
    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000); // 30 seconds
        factory.setReadTimeout(60000);    // 60 seconds
        factory.setBufferRequestBody(true);
        return factory;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
} 