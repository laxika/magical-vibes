package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/** Puts the counter placement event's counter kind and amount on this effect's source. */
public record PutSameCountersOnSourceEffect(
        CounterType counterType,
        int amount,
        UUID targetPermanentId,
        UUID placingPlayerId,
        boolean requiresNonKree) implements CardEffect {

    /** Marker constructor used by card definitions; the event binds the payload before resolution. */
    public PutSameCountersOnSourceEffect() {
        this(null, 0, null, null, false);
    }

    /** Marker constructor for effects that exclude Kree creatures from the copied event. */
    public PutSameCountersOnSourceEffect(boolean requiresNonKree) {
        this(null, 0, null, null, requiresNonKree);
    }

    public PutSameCountersOnSourceEffect(CounterType counterType, int amount, UUID targetPermanentId) {
        this(counterType, amount, targetPermanentId, null, false);
    }
}
