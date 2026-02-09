package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.networking.model.MessageType;

public record PriorityUpdatedMessage(MessageType type, Long priorityPlayerId) {

    public PriorityUpdatedMessage(Long priorityPlayerId) {
        this(MessageType.PRIORITY_UPDATED, priorityPlayerId);
    }
}
