package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Puts counters on each target creature equal to that creature's current color count. */
public record PutCountersOnEachTargetCreatureEqualToColorCountEffect(CounterType counterType)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
