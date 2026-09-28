package com.github.laxika.magicalvibes.model.effect;

/**
 * Flips one coin for each opponent of the resolving ability's controller.
 * The corresponding branch resolves once for each opponent; the loss branch receives that
 * opponent through the entry's triggering-player target context.
 */
public record FlipCoinForEachOpponentEffect(CardEffect winEffect, CardEffect lossEffect)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        TargetSpec winSpec = winEffect == null ? TargetSpec.NONE : winEffect.targetSpec();
        if (winSpec.declaredTarget() != null) {
            return winSpec;
        }
        return lossEffect == null ? TargetSpec.NONE : lossEffect.targetSpec();
    }
}
