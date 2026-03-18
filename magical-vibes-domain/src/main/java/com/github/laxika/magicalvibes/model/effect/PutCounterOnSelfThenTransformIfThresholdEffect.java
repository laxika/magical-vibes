package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Put a counter of the specified type on this permanent, then if the counter count
 * reaches the given threshold, remove all counters of that type and transform.
 * Used by Ludevic's Test Subject and similar cards.
 */
public record PutCounterOnSelfThenTransformIfThresholdEffect(
        CounterType counterType,
        int threshold
) implements CardEffect {

    @Override
    public boolean isSelfTargeting() { return true; }
}
