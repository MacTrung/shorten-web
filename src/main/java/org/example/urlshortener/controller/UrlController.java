package org.example.urlshortener.controller;

import org.example.urlshortener.model.Url;
import org.example.urlshortener.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;



@Controller
public class UrlController {
    @Autowired
    private UrlService service;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/shorten-web")
    public String shortenWeb(@RequestParam("originalUrl") String originalUrl, Model model) {
        if (!service.isValidUrl(originalUrl)) {
            model.addAttribute("message", "Invalid URL format!");
            return "index";
        }

        Url url = service.shortenUrl(originalUrl);

        boolean isExisting = service.getOriginalUrl(url.getShortCode())
                .get().equalsIgnoreCase(service.normalizeUrl(originalUrl));

        String shortUrl = "http://localhost:8081/" + url.getShortCode();

        if (isExisting) {
            model.addAttribute("message", "This URL has already been shortened.");
        }

        model.addAttribute("shortUrl", shortUrl);
        return "index";
    }

    @PostMapping("/shorten")
    @ResponseBody
    public ResponseEntity<String> shorten(@RequestBody String originalUrl) {
        if (!service.isValidUrl(originalUrl)) {
            return ResponseEntity.badRequest().body("Invalid URL format!");
        }

        Url url = service.shortenUrl(originalUrl);
        boolean isExisting = service.getOriginalUrl(url.getShortCode())
                .get().equalsIgnoreCase(service.normalizeUrl(originalUrl));

        String result = "http://localhost:8081/" + url.getShortCode();
        if (isExisting) {
            return ResponseEntity.ok("Link đã từng đc rút gọn: " + result);
        } else {
            return ResponseEntity.ok("đường Link rút gọn mới đã đc tạo: " + result);
        }
    }

    @GetMapping("/{code}")
    public RedirectView redirect(@PathVariable String code) {
        return service.getOriginalUrl(code)
                .map(RedirectView::new)
                .orElseThrow(() -> new RuntimeException("ko tim thấy đường dẫn"));
    }
}
