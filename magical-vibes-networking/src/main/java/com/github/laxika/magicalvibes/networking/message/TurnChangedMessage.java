package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;
import com.github.laxika.magicalvibes.model.TurnStep;

import java.util.UUID;

public record TurnChangedMessage(MessageType type, UUID priorityPlayerId, TurnStep currentStep,
                                 UUID activePlayerId, int turnNumber) {

    public TurnChangedMessage(UUID priorityPlayerId, TurnStep currentStep, UUID activePlayerId, int turnNumber) {
        this(MessageType.TURN_CHANGED, priorityPlayerId, currentStep, activePlayerId, turnNumber);
    }
}
