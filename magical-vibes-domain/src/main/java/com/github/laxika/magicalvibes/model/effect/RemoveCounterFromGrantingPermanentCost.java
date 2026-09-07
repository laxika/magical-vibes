package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Cost that removes counters from the permanent that granted the activated ability rather than
 * from the permanent activating it. This covers Aura-granted abilities whose text names the Aura.
 */
public record RemoveCounterFromGrantingPermanentCost(int count, CounterType counterType)
        implements CostEffect {

    public RemoveCounterFromGrantingPermanentCost() {
        this(1, CounterType.ANY);
    }

    public RemoveCounterFromGrantingPermanentCost(int count) {
        this(count, CounterType.ANY);
    }

    @Override
    public int sourceCountersRemoved() {
        return count;
    }
}
