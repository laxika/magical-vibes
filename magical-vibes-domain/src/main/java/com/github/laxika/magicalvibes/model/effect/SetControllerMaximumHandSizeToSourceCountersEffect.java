package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;

/**
 * Sets the controller's maximum hand size to the number of the specified counters on this source
 * permanent.
 */
public record SetControllerMaximumHandSizeToSourceCountersEffect(CounterType counterType)
        implements ControllerMaxHandSizeEffect {

    @Override
    public int applyToMaximumHandSize(int currentMax) {
        return currentMax;
    }

    @Override
    public int applyToMaximumHandSize(int currentMax, Permanent source) {
        return source.getCounterCount(counterType);
    }
}
