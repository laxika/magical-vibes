package com.github.laxika.magicalvibes.model.effect;

/** Grants a static effect to the targeted player until any player planeswalks. */
public record GrantStaticEffectToTargetPlayerUntilPlaneswalkEffect(CardEffect staticEffect)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }
}
