package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Doubling Season's static replacement effect: doubles any counters put on permanents its
 * controller controls.
 */
public record DoubleCountersOnControlledPermanentsEffect() implements CounterReplacementEffect {

    @Override
    public int replace(CounterType counterType, int count) {
        return count > 0 ? count * 2 : count;
    }
}
