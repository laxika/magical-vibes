package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.UUID;

/** Puts the counter placement event's counter kind and amount on this effect's source. */
public record PutSameCountersOnSourceEffect(CounterType counterType, int amount, UUID targetPermanentId)
        implements CardEffect {

    /** Marker constructor used by card definitions; the event binds the payload before resolution. */
    public PutSameCountersOnSourceEffect() {
        this(null, 0, null);
    }
}
