package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;

import java.util.UUID;

/**
 * Delayed sacrifice checked at the beginning of the next end step. The mana-value condition is
 * intentionally retained until that point because the card text checks it as the delayed trigger
 * resolves.
 */
public record DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtMost(
        UUID permanentId,
        UUID controllerId,
        int maxManaValue,
        Card sourceCard
) implements DelayedAction {
    public DelayedSacrificeTargetPermanentAtEndStepIfManaValueAtMost(
            UUID permanentId, UUID controllerId, int maxManaValue) {
        this(permanentId, controllerId, maxManaValue, null);
    }
}
