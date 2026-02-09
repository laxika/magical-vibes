package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;

public record MulliganResolvedMessage(MessageType type, String playerName, boolean kept, int mulliganCount) {

    public MulliganResolvedMessage(String playerName, boolean kept, int mulliganCount) {
        this(MessageType.MULLIGAN_RESOLVED, playerName, kept, mulliganCount);
    }
}
