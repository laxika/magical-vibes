package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

public record GameLogEntryMessage(MessageType type, String message) {

    public GameLogEntryMessage(String message) {
        this(MessageType.GAME_LOG_ENTRY, message);
    }
}
