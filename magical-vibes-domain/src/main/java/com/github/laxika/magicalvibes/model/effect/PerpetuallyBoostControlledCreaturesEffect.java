package com.github.laxika.magicalvibes.model.effect;

/** Perpetually boosts every creature currently controlled by the effect controller. */
public record PerpetuallyBoostControlledCreaturesEffect(int powerBoost, int toughnessBoost)
        implements CardEffect {
}
