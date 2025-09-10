CREATE TABLE url
(
    id           BIGINT AUTO_INCREMENT NOT NULL,
    original_url VARCHAR(2048)         NULL,
    short_code   VARCHAR(255)          NULL,
    created_at   datetime              NULL,
    CONSTRAINT pk_url PRIMARY KEY (id)
);

ALTER TABLE url
    ADD CONSTRAINT uc_url_originalurl UNIQUE (original_url);

ALTER TABLE url
    ADD CONSTRAINT uc_url_shortcode UNIQUE (short_code);