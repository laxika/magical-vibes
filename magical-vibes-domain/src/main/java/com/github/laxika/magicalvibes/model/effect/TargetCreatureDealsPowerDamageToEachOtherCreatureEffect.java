package com.github.laxika.magicalvibes.model.effect;

/**
 * The targeted creature deals damage equal to its power to every other creature on the
 * battlefield. The targeted creature is the damage source and is not damaged by this effect.
 */
public record TargetCreatureDealsPowerDamageToEachOtherCreatureEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
