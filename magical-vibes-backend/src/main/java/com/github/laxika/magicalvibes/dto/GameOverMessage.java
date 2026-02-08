package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.model.MessageType;

public record GameOverMessage(MessageType type, Long winnerId, String winnerName) {
    public GameOverMessage(Long winnerId, String winnerName) {
        this(MessageType.GAME_OVER, winnerId, winnerName);
    }
}
