package org.example.urlshortener.dto;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class ShortenRequest {
    private String originalUrl;
}
