package com.github.laxika.magicalvibes.model.effect;

/** Perpetually adds a static effect to a target creature's runtime card. */
public record PerpetuallyGrantStaticEffectToTargetCreatureEffect(CardEffect staticEffect)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
