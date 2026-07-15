package com.github.laxika.magicalvibes.model.effect;

/**
 * Target creature deals damage to itself equal to its power.
 * The target creature is both the damage source and recipient.
 */
public record TargetCreatureDealsPowerDamageToSelfEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.CREATURE);
    }
}
