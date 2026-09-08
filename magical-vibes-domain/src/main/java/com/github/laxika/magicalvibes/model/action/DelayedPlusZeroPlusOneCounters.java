package com.github.laxika.magicalvibes.model.action;

import com.github.laxika.magicalvibes.model.Card;
import java.util.UUID;

/**
 * Delayed trigger: {@code totalCounters} +0/+1 counters to put on {@code permanentId} at the beginning
 * of the next end step (Sacred Boon: "put a +0/+1 counter on that creature for each 1 damage prevented
 * this way"). Keyed-accumulator semantics (at most one entry per permanent) are preserved by
 * {@code GameData.addDelayedPlusZeroPlusOneCounters}.
 */
public record DelayedPlusZeroPlusOneCounters(UUID permanentId, int totalCounters,
                                              UUID controllerId, Card sourceCard,
                                              UUID shieldId, int remainingShield)
        implements DelayedAction {

    public DelayedPlusZeroPlusOneCounters(UUID permanentId, int totalCounters) {
        this(permanentId, totalCounters, null, null, UUID.randomUUID(), 0);
    }
}
