package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.model.AiDifficulty;

public record CreateGameRequest(String gameName, String deckId, Boolean vsAi, String aiDeckId, AiDifficulty aiDifficulty) {
}
