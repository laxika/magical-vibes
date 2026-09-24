package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * During resolution, repeatedly choose permanents from among all battlefields and remove up to
 * {@code maxAmount} counters of the specified type in total.
 */
public record RemoveUpToCountersFromAllPermanentsEffect(CounterType counterType, int maxAmount)
        implements CardEffect {

    public RemoveUpToCountersFromAllPermanentsEffect {
        if (counterType == null) {
            throw new IllegalArgumentException("counterType must not be null");
        }
        if (maxAmount < 0) {
            throw new IllegalArgumentException("maxAmount must not be negative");
        }
    }
}
