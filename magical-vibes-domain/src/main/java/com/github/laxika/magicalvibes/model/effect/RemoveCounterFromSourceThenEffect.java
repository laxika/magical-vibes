package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Removes one counter from the source permanent and, if a counter was removed, puts a reflexive
 * triggered ability with {@code thenEffect} on the stack.
 *
 * <p>This is useful for "you may remove a counter from this. When you do, ..." abilities. The
 * follow-up is deliberately not exposed as the outer effect's target: a reflexive trigger chooses
 * its target only after the counter has actually been removed.
 *
 * @param counterType the counter kind to remove; {@link CounterType#ANY} removes one present kind
 * @param thenEffect the effect of the reflexive triggered ability
 */
public record RemoveCounterFromSourceThenEffect(CounterType counterType, CardEffect thenEffect)
        implements CardEffect {
}
