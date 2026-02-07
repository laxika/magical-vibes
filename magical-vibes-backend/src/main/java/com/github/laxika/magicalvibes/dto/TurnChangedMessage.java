package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.model.MessageType;
import com.github.laxika.magicalvibes.model.TurnStep;

public record TurnChangedMessage(MessageType type, Long priorityPlayerId, TurnStep currentStep,
                                 Long activePlayerId, int turnNumber) {

    public TurnChangedMessage(Long priorityPlayerId, TurnStep currentStep, Long activePlayerId, int turnNumber) {
        this(MessageType.TURN_CHANGED, priorityPlayerId, currentStep, activePlayerId, turnNumber);
    }
}
