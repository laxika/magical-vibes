package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Delayed triggered ability that sacrifices its source at an end step. */
public record SacrificeSelfAtNextEndStepTrigger(
        UUID permanentId,
        UUID controllerId,
        Card sourceCard,
        Integer registeredTurnNumber
) implements DelayedAction {

    public SacrificeSelfAtNextEndStepTrigger(UUID permanentId, UUID controllerId, Card sourceCard) {
        this(permanentId, controllerId, sourceCard, null);
    }
}
