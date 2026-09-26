package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

import java.util.Objects;

/**
 * Flips a dynamic number of coins, resolving one effect for each win and another for each loss.
 * The all-wins effect resolves only when exactly five coins were flipped and all five were won.
 */
public record FlipCoinsWithResultEffectsEffect(DynamicAmount coins, CardEffect winEffect,
                                               CardEffect lossEffect, CardEffect allWinsEffect)
        implements CardEffect {

    public FlipCoinsWithResultEffectsEffect {
        Objects.requireNonNull(coins, "coins");
        Objects.requireNonNull(winEffect, "winEffect");
        Objects.requireNonNull(lossEffect, "lossEffect");
        Objects.requireNonNull(allWinsEffect, "allWinsEffect");
    }

    @Override
    public TargetSpec targetSpec() {
        TargetSpec targetSpec = winEffect.targetSpec();
        if (targetSpec.declaredTarget() != null) {
            return targetSpec;
        }
        targetSpec = lossEffect.targetSpec();
        if (targetSpec.declaredTarget() != null) {
            return targetSpec;
        }
        return allWinsEffect.targetSpec();
    }
}
