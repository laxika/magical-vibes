package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Condition;

import java.util.List;
import java.util.UUID;

/** Delayed trigger that checks a condition before exiling the recorded permanents. */
public record DelayedExileCreatedPermanentsAtEndStep(
        UUID controllerId,
        List<UUID> permanentIds,
        Condition condition,
        Card sourceCard
) implements DelayedAction {
}
