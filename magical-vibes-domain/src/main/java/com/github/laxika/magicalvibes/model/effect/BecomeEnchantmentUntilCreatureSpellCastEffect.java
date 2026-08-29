package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the targeted creature an enchantment and removes all its abilities until any player casts
 * a creature spell.
 */
public record BecomeEnchantmentUntilCreatureSpellCastEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
