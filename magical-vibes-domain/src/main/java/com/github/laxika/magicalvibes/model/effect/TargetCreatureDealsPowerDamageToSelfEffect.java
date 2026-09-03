package com.github.laxika.magicalvibes.model.effect;

/**
 * Target creature deals damage to itself equal to a multiple of its power.
 * The target creature is both the damage source and recipient.
 */
public record TargetCreatureDealsPowerDamageToSelfEffect(int powerMultiplier) implements CardEffect {

    public TargetCreatureDealsPowerDamageToSelfEffect {
        if (powerMultiplier < 1) {
            throw new IllegalArgumentException("Power multiplier must be positive");
        }
    }

    public TargetCreatureDealsPowerDamageToSelfEffect() {
        this(1);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
