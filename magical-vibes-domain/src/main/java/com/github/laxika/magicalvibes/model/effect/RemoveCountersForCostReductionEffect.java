package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Optional additional cast cost that removes any number of counters from among creatures the
 * caster controls, reducing this spell's generic cost for each counter removed.
 */
public record RemoveCountersForCostReductionEffect(int reductionPerCounter, CounterType counterType)
        implements CardEffect {

    public RemoveCountersForCostReductionEffect(int reductionPerCounter) {
        this(reductionPerCounter, CounterType.PLUS_ONE_PLUS_ONE);
    }
}
