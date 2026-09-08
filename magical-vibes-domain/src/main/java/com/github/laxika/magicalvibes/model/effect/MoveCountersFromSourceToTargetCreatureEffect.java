package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * "Move any number of counters from this permanent onto target creature."
 *
 * @param counterType the kind of counter that may be moved
 */
public record MoveCountersFromSourceToTargetCreatureEffect(CounterType counterType)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
