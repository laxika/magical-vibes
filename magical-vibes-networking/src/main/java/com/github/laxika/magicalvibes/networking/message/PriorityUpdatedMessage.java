package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.UUID;

public record PriorityUpdatedMessage(MessageType type, UUID priorityPlayerId) {

    public PriorityUpdatedMessage(UUID priorityPlayerId) {
        this(MessageType.PRIORITY_UPDATED, priorityPlayerId);
    }
}
