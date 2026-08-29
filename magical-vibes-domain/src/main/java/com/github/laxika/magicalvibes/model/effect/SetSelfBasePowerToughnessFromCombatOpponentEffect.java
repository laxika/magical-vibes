package com.github.laxika.magicalvibes.model.effect;

/**
 * Sets the source creature's base power and toughness to the current power and toughness of its
 * combat opponent until end of turn. The combat-trigger pipeline carries that opponent as the
 * stack entry's non-targeting target.
 */
public record SetSelfBasePowerToughnessFromCombatOpponentEffect()
        implements CardEffect, CombatOpponentReferencingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
