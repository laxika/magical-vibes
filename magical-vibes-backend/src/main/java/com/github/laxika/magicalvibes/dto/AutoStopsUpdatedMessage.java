package com.github.laxika.magicalvibes.dto;

import com.github.laxika.magicalvibes.networking.model.MessageType;
import com.github.laxika.magicalvibes.model.TurnStep;

import java.util.List;

public record AutoStopsUpdatedMessage(MessageType type, List<TurnStep> autoStopSteps) {

    public AutoStopsUpdatedMessage(List<TurnStep> autoStopSteps) {
        this(MessageType.AUTO_STOPS_UPDATED, autoStopSteps);
    }
}
