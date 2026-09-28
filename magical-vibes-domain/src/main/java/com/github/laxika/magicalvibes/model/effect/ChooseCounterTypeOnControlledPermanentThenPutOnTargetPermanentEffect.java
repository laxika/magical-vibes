package com.github.laxika.magicalvibes.model.effect;

/**
 * Chooses a counter kind on a permanent the controller controls, then puts one
 * counter of that kind on the targeted permanent if it does not already have one.
 */
public record ChooseCounterTypeOnControlledPermanentThenPutOnTargetPermanentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
