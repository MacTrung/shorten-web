package org.example.urlshortener.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.urlshortener.dto.ShortenRequest;
import org.example.urlshortener.dto.ShortenResponse;
import org.example.urlshortener.model.Url;
import org.example.urlshortener.service.UrlService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


@Controller
public class UrlController {
    private static final Logger log = LoggerFactory.getLogger(UrlController.class);
    private final UrlService service;

    public UrlController(UrlService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @PostMapping("/shorten")
    public String shorten(@RequestParam("originalUrl") String originalUrl, HttpServletRequest request, Model model) {
        if (!service.isValidUrl(originalUrl)) {
            log.warn("Invalid URL format submitted: {}", originalUrl);
            model.addAttribute("message", "Invalid URL format!");
            return "index";
        }

        Url url = service.shortenUrl(originalUrl);
        String shortUrl = buildShortUrl(request, url.getShortCode());
        log.info("URL shortened: {} -> {}", originalUrl, shortUrl);

        model.addAttribute("shortUrl", shortUrl);
        return "index";
    }

    @PostMapping("/api/shorten")
    @ResponseBody
    public ResponseEntity<ShortenResponse> shortenApi(@RequestBody ShortenRequest shortenRequest, HttpServletRequest request) {
        if (!service.isValidUrl(shortenRequest.getOriginalUrl())) {
            log.warn("Invalid URL format submitted via API: {}", shortenRequest.getOriginalUrl());
            return ResponseEntity.badRequest().build();
        }

        Url url = service.shortenUrl(shortenRequest.getOriginalUrl());
        String shortUrl = buildShortUrl(request, url.getShortCode());
        log.info("URL shortened via API: {} -> {}", shortenRequest.getOriginalUrl(), shortUrl);

        return ResponseEntity.ok(new ShortenResponse(shortUrl));
    }

    @GetMapping("/{code}")
    public ResponseEntity<Void> redirect(@PathVariable String code) {
        return service.getOriginalUrl(code)
                .<ResponseEntity<Void>>map(originalUrl -> {
                    log.info("Redirecting {} to {}", code, originalUrl);
                    return ResponseEntity.status(HttpStatus.FOUND)
                            .location(URI.create(originalUrl))
                            .build();
                })
                .orElseGet(() -> {
                    log.warn("Short code not found: {}", code);
                    return ResponseEntity.notFound().build();
                });
    }

    private String buildShortUrl(HttpServletRequest request, String shortCode) {
        return request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/" + shortCode;
    }
}
