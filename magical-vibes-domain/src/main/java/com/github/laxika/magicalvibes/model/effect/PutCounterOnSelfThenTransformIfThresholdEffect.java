package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Put a counter of the specified type on this permanent, then if the counter count
 * reaches the given threshold, remove all counters of that type and transform.
 * <p>
 * When {@code optional} is true, the player is prompted ("you may") before the
 * counters are removed and the permanent transforms (e.g. Primal Amulet).
 * Used by Ludevic's Test Subject, Primal Amulet, and similar cards.
 */
public record PutCounterOnSelfThenTransformIfThresholdEffect(
        CounterType counterType,
        int threshold,
        boolean optional
) implements CardEffect {

    public PutCounterOnSelfThenTransformIfThresholdEffect(CounterType counterType, int threshold) {
        this(counterType, threshold, false);
    }

    @Override
    public boolean isSelfTargeting() { return true; }
}
