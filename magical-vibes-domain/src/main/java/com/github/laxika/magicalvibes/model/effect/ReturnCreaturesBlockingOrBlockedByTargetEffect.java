package com.github.laxika.magicalvibes.model.effect;

/** Returns every creature blocking or blocked by the targeted creature to its owner's hand. */
public record ReturnCreaturesBlockingOrBlockedByTargetEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
