package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives the targeted permanent a permanent layer-6 static effect independent of the resolving
 * source permanent.
 */
public record GrantStaticEffectToTargetEffect(CardEffect staticEffect) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
