package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.model.GameStatus;

import java.util.UUID;

public record LobbyGame(UUID id, String gameName, String createdByUsername,
                         int playerCount, GameStatus status, boolean allRandom, boolean planechase) {
    public LobbyGame(UUID id, String gameName, String createdByUsername, int playerCount, GameStatus status, boolean allRandom) {
        this(id, gameName, createdByUsername, playerCount, status, allRandom, false);
    }

}
