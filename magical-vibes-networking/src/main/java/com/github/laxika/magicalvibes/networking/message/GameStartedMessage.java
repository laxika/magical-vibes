package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;
import com.github.laxika.magicalvibes.model.TurnStep;

import java.util.UUID;

public record GameStartedMessage(MessageType type, UUID activePlayerId, int turnNumber,
                                  TurnStep currentStep, UUID priorityPlayerId) {

    public GameStartedMessage(UUID activePlayerId, int turnNumber, TurnStep currentStep, UUID priorityPlayerId) {
        this(MessageType.GAME_STARTED, activePlayerId, turnNumber, currentStep, priorityPlayerId);
    }
}
