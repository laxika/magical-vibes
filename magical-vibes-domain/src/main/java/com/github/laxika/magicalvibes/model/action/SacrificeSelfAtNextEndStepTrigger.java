package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/** Delayed triggered ability that sacrifices its source at the beginning of the next end step. */
public record SacrificeSelfAtNextEndStepTrigger(UUID permanentId, UUID controllerId, Card sourceCard)
        implements DelayedAction {
}
