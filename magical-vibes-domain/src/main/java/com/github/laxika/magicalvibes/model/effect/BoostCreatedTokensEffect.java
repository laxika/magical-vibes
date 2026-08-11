package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Gives the tokens created earlier in the same spell or ability resolution a temporary power and
 * toughness boost.
 */
public record BoostCreatedTokensEffect(DynamicAmount powerBoost, DynamicAmount toughnessBoost)
        implements CardEffect {

    public BoostCreatedTokensEffect(int powerBoost, int toughnessBoost) {
        this(new Fixed(powerBoost), new Fixed(toughnessBoost));
    }
}
