package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Death trigger for "When this creature dies, draw a card for each {@code counterType} counter on it"
 * (e.g. Dusk Urchins: draw a card for each -1/-1 counter on it).
 * <p>
 * Placed on the {@code ON_DEATH} or {@code ON_ANY_CREATURE_DIES} slot. The death-trigger collector snapshots the dying permanent's
 * count of {@code counterType} at the moment of death and resolves into a plain
 * {@link DrawCardEffect} for that many cards, reusing the standard draw handler.
 *
 * @param counterType the counter type to count on the dying creature, or {@code null} to count
 *                    all counters
 * @param thenEffect optional effect to resolve after the draw
 */
public record DrawCardForEachDyingSourceCounterEffect(CounterType counterType, CardEffect thenEffect)
        implements CardEffect {

    public DrawCardForEachDyingSourceCounterEffect(CounterType counterType) {
        this(counterType, null);
    }
}
