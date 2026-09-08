package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Death trigger for "When this creature dies, if it had a counter on it, draw a card".
 *
 * <p>The death-trigger collector checks the dying permanent's counter count at the moment of
 * death and queues a plain {@link DrawCardEffect} when at least one matching counter was present.
 *
 * @param counterType the counter type checked on the dying creature
 */
public record DrawCardIfDyingSourceHadCounterEffect(CounterType counterType) implements CardEffect {
}
