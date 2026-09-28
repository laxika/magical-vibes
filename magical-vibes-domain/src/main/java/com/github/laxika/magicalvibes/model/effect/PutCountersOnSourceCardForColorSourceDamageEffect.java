package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Trigger marker for putting counters on the watching permanent when a source of the specified
 * color controlled by the watcher deals damage. The damage trigger collector turns this marker
 * into a {@link PutCountersOnSourceCardEffect} when the event occurs.
 */
public record PutCountersOnSourceCardForColorSourceDamageEffect(
        CardColor color,
        CounterType counterType,
        int count
) implements CardEffect {

    public PutCountersOnSourceCardForColorSourceDamageEffect(CardColor color, CounterType counterType) {
        this(color, counterType, 1);
    }
}
