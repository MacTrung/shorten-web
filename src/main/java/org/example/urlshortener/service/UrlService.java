package org.example.urlshortener.service;

import org.example.urlshortener.model.Url;
import org.example.urlshortener.repository.UrlRepository;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.UUID;

@Service
public class UrlService {

    private static final String HTTP_PREFIX = "http://";
    private static final String HTTPS_PREFIX = "https://";

    private final UrlRepository repository;

    public UrlService(UrlRepository repository) {
        this.repository = repository;
    }

    public Url shortenUrl(String originalUrl) {
        String normalizedUrl = normalizeUrl(originalUrl);

        return repository.findByOriginalUrl(normalizedUrl)
                .orElseGet(() -> createAndSaveUrl(normalizedUrl));
    }

    public Optional<String> getOriginalUrl(String code) {
        return repository.findByShortCode(code)
                .map(Url::getOriginalUrl);
    }

    public String normalizeUrl(String url) {
        String fixedUrl = addProtocolIfMissing(url).toLowerCase();
        return removeTrailingSlash(fixedUrl);
    }

    public boolean isValidUrl(String url) {
        try {
            URI uri = new URI(normalizeUrl(url));
            return uri.getHost() != null && uri.getHost().contains(".");
        } catch (URISyntaxException e) {
            return false;
        }
    }
    
    private Url createAndSaveUrl(String normalizedUrl) {
        Url url = new Url();
        url.setOriginalUrl(normalizedUrl);
        url.setShortCode(generateShortCode());
        return repository.save(url);
    }

    private String generateShortCode() {
        String shortCode;
        do {
            shortCode = UUID.randomUUID()
                    .toString()
                    .substring(0, 8);
        } while (repository.findByShortCode(shortCode).isPresent());
        return shortCode;
    }

    private String addProtocolIfMissing(String url) {
        if (!url.startsWith(HTTP_PREFIX) && !url.startsWith(HTTPS_PREFIX)) {
            return HTTPS_PREFIX + url;
        }
        return url;
    }

    private String removeTrailingSlash(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
