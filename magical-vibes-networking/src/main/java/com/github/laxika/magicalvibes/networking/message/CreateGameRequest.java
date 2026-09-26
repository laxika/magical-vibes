package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.model.AiDifficulty;

public record CreateGameRequest(String gameName, String deckId, Boolean vsAi, String aiDeckId, AiDifficulty aiDifficulty,
                                Boolean allRandom, String randomSet, Boolean planechase, com.github.laxika.magicalvibes.model.DeckFormat format) {
    public CreateGameRequest {
        format = format == null ? com.github.laxika.magicalvibes.model.DeckFormat.CASUAL : format;
    }
    public CreateGameRequest(String gameName, String deckId, Boolean vsAi, String aiDeckId, AiDifficulty aiDifficulty, Boolean allRandom, String randomSet, Boolean planechase) {
        this(gameName, deckId, vsAi, aiDeckId, aiDifficulty, allRandom, randomSet, planechase, com.github.laxika.magicalvibes.model.DeckFormat.CASUAL);
    }
    public CreateGameRequest(String gameName, String deckId, Boolean vsAi, String aiDeckId, AiDifficulty aiDifficulty, Boolean allRandom, String randomSet) {
        this(gameName, deckId, vsAi, aiDeckId, aiDifficulty, allRandom, randomSet, false);
    }

}
