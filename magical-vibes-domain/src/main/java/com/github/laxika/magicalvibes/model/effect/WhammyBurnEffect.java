package com.github.laxika.magicalvibes.model.effect;

/** Reveals a separate whammy deck until an Island is found or the controller stops. */
public record WhammyBurnEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.anyTarget());
    }
}
