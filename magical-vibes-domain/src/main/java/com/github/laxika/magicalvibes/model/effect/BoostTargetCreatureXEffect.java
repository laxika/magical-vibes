package com.github.laxika.magicalvibes.model.effect;

/**
 * Boosts target creature by a multiple of X until end of turn.
 * Actual power boost = powerMultiplier * X, toughness boost = toughnessMultiplier * X.
 * For example, Untamed Might uses (1, 1) for +X/+X.
 */
public record BoostTargetCreatureXEffect(
        int powerMultiplier,
        int toughnessMultiplier
) implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
