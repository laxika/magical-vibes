package com.github.laxika.magicalvibes.model.effect;

/**
 * Places one +1/+1 counter on a target creature for each counter on a permanent that just left
 * the battlefield. The trigger collector binds the total concrete counter count from the event
 * snapshot before the source permanent leaves the battlefield.
 */
public record PutPlusOnePlusOneCountersOnTargetForEachDyingSourceCounterEffect(int count)
        implements CardEffect, DyingCreatureCounterAwareEffect {

    public PutPlusOnePlusOneCountersOnTargetForEachDyingSourceCounterEffect() {
        this(0);
    }

    @Override
    public CardEffect boundToDyingCreatureCounterCount(int counterCount) {
        return new PutPlusOnePlusOneCountersOnTargetForEachDyingSourceCounterEffect(counterCount);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
