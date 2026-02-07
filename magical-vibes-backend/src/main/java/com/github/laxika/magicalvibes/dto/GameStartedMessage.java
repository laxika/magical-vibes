package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.model.MessageType;
import com.github.laxika.magicalvibes.model.TurnStep;

public record GameStartedMessage(MessageType type, Long activePlayerId, int turnNumber,
                                  TurnStep currentStep, Long priorityPlayerId) {

    public GameStartedMessage(Long activePlayerId, int turnNumber, TurnStep currentStep, Long priorityPlayerId) {
        this(MessageType.GAME_STARTED, activePlayerId, turnNumber, currentStep, priorityPlayerId);
    }
}
