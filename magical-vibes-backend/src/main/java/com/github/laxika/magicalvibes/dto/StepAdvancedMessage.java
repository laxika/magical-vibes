package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.model.MessageType;
import com.github.laxika.magicalvibes.model.TurnStep;

public record StepAdvancedMessage(MessageType type, Long priorityPlayerId, TurnStep currentStep) {

    public StepAdvancedMessage(Long priorityPlayerId, TurnStep currentStep) {
        this(MessageType.STEP_ADVANCED, priorityPlayerId, currentStep);
    }
}
