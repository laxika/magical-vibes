package com.github.laxika.magicalvibes.model.effect;

/**
 * Combat trigger: destroy the creature the source Equipment was attached to and its combat
 * opponent. The attached creature is carried by the triggered stack entry's triggering permanent
 * ID so moving the Equipment after the ability triggers does not change which creature is destroyed.
 */
public record DestroyEquippedCreatureAndCombatOpponentEffect()
        implements CardEffect, CombatOpponentReferencingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
