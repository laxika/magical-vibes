package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.UUID;

public record GameOverMessage(MessageType type, UUID winnerId, String winnerName) {
    public GameOverMessage(UUID winnerId, String winnerName) {
        this(MessageType.GAME_OVER, winnerId, winnerName);
    }
}
