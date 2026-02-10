package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;
import com.github.laxika.magicalvibes.model.TurnStep;

import java.util.UUID;

public record StepAdvancedMessage(MessageType type, UUID priorityPlayerId, TurnStep currentStep) {

    public StepAdvancedMessage(UUID priorityPlayerId, TurnStep currentStep) {
        this(MessageType.STEP_ADVANCED, priorityPlayerId, currentStep);
    }
}
