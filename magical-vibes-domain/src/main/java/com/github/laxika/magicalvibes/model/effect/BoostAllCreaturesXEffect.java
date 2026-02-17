package com.github.laxika.magicalvibes.model.effect;

/**
 * Boosts ALL creatures on the battlefield (both players) by a multiple of X.
 * Actual power boost = powerMultiplier * X, toughness boost = toughnessMultiplier * X.
 * For example, Flowstone Slide uses (1, -1) for +X/-X.
 */
public record BoostAllCreaturesXEffect(int powerMultiplier, int toughnessMultiplier) implements CardEffect {
}
