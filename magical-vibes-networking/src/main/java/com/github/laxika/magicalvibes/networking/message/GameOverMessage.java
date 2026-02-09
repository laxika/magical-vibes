package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

public record GameOverMessage(MessageType type, Long winnerId, String winnerName) {
    public GameOverMessage(Long winnerId, String winnerName) {
        this(MessageType.GAME_OVER, winnerId, winnerName);
    }
}
