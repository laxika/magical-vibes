package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * {@code ON_DEATH}: "exile it if it had a [counter] on it. Otherwise, return it to the battlefield
 * under your control and put a [counter] on it" (Bogardan Phoenix).
 *
 * <p>The death-trigger collector snapshots whether the dying permanent had at least one counter of
 * {@code counterType} and pushes either {@link ExileSourceCardFromGraveyardEffect} or a self
 * {@link ReturnCardFromGraveyardEffect} that places one of that counter on entry.
 *
 * @param counterType counter checked at death and placed on the return branch
 */
public record ExileIfHadCounterElseReturnWithCounterEffect(CounterType counterType) implements CardEffect {
}
