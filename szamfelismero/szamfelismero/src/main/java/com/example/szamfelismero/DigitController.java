package com.example.szamfelismero;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.ui.Model;
import java.io.IOException;



@Controller
public class DigitController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/predict")
    public String predict(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        if (file.isEmpty()) {
            return "redirect:/";
        }

        // 1. Felkészíti a képet a továbbküldésre
        RestTemplate restTemplate = new RestTemplate();
        String pythonUrl = "http://localhost:8000/predict";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // 2. Meghívja a Python API-t
        try {
            ResponseEntity<PythonResponse> response = restTemplate.postForEntity(
                    pythonUrl, requestEntity, PythonResponse.class);

            // 3. Az eredményt átadja a HTML-nek
            model.addAttribute("digit", response.getBody().getDigit());
            model.addAttribute("confidence", response.getBody().getConfidence());
        } catch (Exception e) {
            model.addAttribute("error", "A Python API nem elérhető. Indítsd el a main.py-t!");
        }

        return "index";
    }

    // Segédosztály a JSON válasz feldolgozásához
    static class PythonResponse {
        private int digit;
        private String confidence;
        public int getDigit() { return digit; }
        public void setDigit(int digit) { this.digit = digit; }
        public String getConfidence() { return confidence; }
        public void setConfidence(String confidence) { this.confidence = confidence; }
    }
}
