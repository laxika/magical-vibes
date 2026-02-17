package com.github.laxika.magicalvibes.networking.message;

public record CreateGameRequest(String gameName, String deckId, Boolean vsAi, String aiDeckId) {
}
