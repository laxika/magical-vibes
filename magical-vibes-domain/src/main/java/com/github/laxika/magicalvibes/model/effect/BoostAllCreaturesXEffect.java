package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Boosts ALL creatures on the battlefield (both players) by a multiple of X.
 * Actual power boost = powerMultiplier * X, toughness boost = toughnessMultiplier * X.
 * For example, Flowstone Slide uses (1, -1) for +X/-X.
 */
public record BoostAllCreaturesXEffect(
        int powerMultiplier,
        int toughnessMultiplier,
        PermanentPredicate filter
) implements CardEffect {

    public BoostAllCreaturesXEffect(int powerMultiplier, int toughnessMultiplier) {
        this(powerMultiplier, toughnessMultiplier, null);
    }
}
