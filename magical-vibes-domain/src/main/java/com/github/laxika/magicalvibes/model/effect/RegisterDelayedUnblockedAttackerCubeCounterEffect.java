package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed trigger for the chosen creature: this turn, when it attacks and isn't
 * blocked, it assigns no combat damage and the ability's source card gets a cube counter.
 */
public record RegisterDelayedUnblockedAttackerCubeCounterEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
