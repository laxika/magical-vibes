package com.github.laxika.magicalvibes.model.effect;

/**
 * Counter target spell or ability unless its controller gets a number of poison counters.
 */
public record CounterUnlessGetsPoisonCountersEffect(int amount)
        implements CounterSpellingEffect, CounterUnlessEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }

    @Override
    public RansomKind ransomKind() {
        return RansomKind.GET_POISON_COUNTERS;
    }

    @Override
    public int ransomMagnitude() {
        return amount;
    }
}
