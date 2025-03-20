package org.example.easyocr;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("org.example.easyocr.mapper")
@SpringBootApplication
public class EasyOcrApplication {

    public static void main(String[] args) {
        SpringApplication.run(EasyOcrApplication.class, args);
    }

}
