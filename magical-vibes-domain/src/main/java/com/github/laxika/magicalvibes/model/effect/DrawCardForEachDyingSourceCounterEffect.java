package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Death trigger for "When this creature dies, draw a card for each {@code counterType} counter on it"
 * (e.g. Dusk Urchins: draw a card for each -1/-1 counter on it).
 * <p>
 * Placed on the {@code ON_DEATH} slot. The death-trigger collector snapshots the dying permanent's
 * count of {@code counterType} at the moment of death and resolves into a plain
 * {@link DrawCardEffect} for that many cards, reusing the standard draw handler.
 *
 * @param counterType the counter type to count on the dying creature
 */
public record DrawCardForEachDyingSourceCounterEffect(CounterType counterType) implements CardEffect {
}
