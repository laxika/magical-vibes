package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.networking.model.MessageType;

public record GameLogEntryMessage(MessageType type, String message) {

    public GameLogEntryMessage(String message) {
        this(MessageType.GAME_LOG_ENTRY, message);
    }
}
