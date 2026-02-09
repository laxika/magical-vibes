package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.networking.model.MessageType;

public record ErrorMessage(MessageType type, String message) {

    public ErrorMessage(String message) {
        this(MessageType.ERROR, message);
    }
}
