package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import java.util.Objects;

/**
 * Flips up to {@code coins} coins, stopping at the first loss, and resolves the reward only when
 * every required flip was won.
 *
 * @param coins       number of logical coin flips to attempt
 * @param allWinsEffect effect to resolve when every attempted flip was won
 */
public record FlipCoinsUntilLoseEffect(DynamicAmount coins, CardEffect allWinsEffect) implements CardEffect {

    public FlipCoinsUntilLoseEffect {
        Objects.requireNonNull(coins, "coins");
        Objects.requireNonNull(allWinsEffect, "allWinsEffect");
    }

    @Override
    public TargetSpec targetSpec() {
        return allWinsEffect.targetSpec();
    }
}
