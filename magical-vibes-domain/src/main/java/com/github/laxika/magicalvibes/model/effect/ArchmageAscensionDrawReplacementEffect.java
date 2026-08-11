package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/** Archmage Ascension's optional draw replacement once it has six quest counters. */
public record ArchmageAscensionDrawReplacementEffect() implements CounterThresholdDrawReplacementEffect {

    @Override
    public CounterType counterType() {
        return CounterType.QUEST;
    }

    @Override
    public int minimumCounters() {
        return 6;
    }
}
