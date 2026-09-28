package com.github.laxika.magicalvibes.model.effect;

/** Perpetually modifies the power and toughness of a target creature spell's runtime card. */
public record PerpetuallyBoostTargetCreatureSpellEffect(int powerBoost, int toughnessBoost)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
