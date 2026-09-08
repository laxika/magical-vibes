package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Has the target player choose a creature they control and put counters on it. */
public record TargetPlayerPutsCounterOnCreatureEffect(CounterType counterType, int count)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
