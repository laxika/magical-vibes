package com.github.laxika.magicalvibes.networking.message;

import com.github.laxika.magicalvibes.networking.model.MessageType;
import com.github.laxika.magicalvibes.model.TurnStep;

public record StepAdvancedMessage(MessageType type, Long priorityPlayerId, TurnStep currentStep) {

    public StepAdvancedMessage(Long priorityPlayerId, TurnStep currentStep) {
        this(MessageType.STEP_ADVANCED, priorityPlayerId, currentStep);
    }
}
