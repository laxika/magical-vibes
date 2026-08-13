package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect that prevents a target creature from blocking for the current combat.
 */
public record CantBlockThisCombatEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
