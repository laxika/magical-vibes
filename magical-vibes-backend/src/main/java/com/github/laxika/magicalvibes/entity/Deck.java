package com.github.laxika.magicalvibes.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "decks")
public class Deck {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(columnDefinition = "VARCHAR(36)")
    private UUID id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_id", nullable = false, columnDefinition = "VARCHAR(36)")
    private UUID userId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "deck_json", nullable = false, columnDefinition = "TEXT")
    private String deckJson;

    public Deck() {
    }

    public Deck(UUID userId, String name, String deckJson) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.name = name;
        this.deckJson = deckJson;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDeckJson() {
        return deckJson;
    }

    public void setDeckJson(String deckJson) {
        this.deckJson = deckJson;
    }
}
