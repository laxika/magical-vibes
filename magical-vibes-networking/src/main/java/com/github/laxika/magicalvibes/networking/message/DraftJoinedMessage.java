package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

import java.util.List;
import java.util.UUID;

public record DraftJoinedMessage(
        MessageType type,
        UUID draftId,
        String draftName,
        String setCode,
        List<String> playerNames,
        String status
) {
    public DraftJoinedMessage(UUID draftId, String draftName, String setCode, List<String> playerNames, String status) {
        this(MessageType.DRAFT_JOINED, draftId, draftName, setCode, playerNames, status);
    }
}
