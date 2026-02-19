package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

public record DraftFinishedMessage(
        MessageType type,
        String winnerName
) {
    public DraftFinishedMessage(String winnerName) {
        this(MessageType.DRAFT_FINISHED, winnerName);
    }
}
