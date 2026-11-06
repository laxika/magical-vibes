package com.github.laxika.magicalvibes.model.effect;

/**
 * Places one +1/+1 counter on a target creature for each counter the permanent that just left
 * the battlefield had: the stack entry's triggering permanent, as it last existed on the battlefield (CR 603.10a).
 */
public record PutPlusOnePlusOneCountersOnTargetForEachDyingSourceCounterEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
