package com.github.laxika.magicalvibes.model.effect;

/** Perpetually modifies the power and toughness of the creature this source blocks or is blocked by. */
public record PerpetuallyBoostCombatOpponentEffect(int powerBoost, int toughnessBoost)
        implements CombatOpponentReferencingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
