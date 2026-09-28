package com.github.laxika.magicalvibes.model.effect;

/**
 * Target creature deals damage equal to its power to each opponent. The targeted creature is the
 * damage source for power, prevention, protection, lifelink, and damage triggers.
 */
public record TargetCreatureDealsPowerDamageToEachOpponentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
