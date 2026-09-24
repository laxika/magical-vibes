package com.github.laxika.magicalvibes.model.effect;

/** Seeks a Dragon from the library and perpetually boosts it and the source by +1/+1. */
public record SeekDragonAndPerpetuallyBoostEffect(int powerBoost, int toughnessBoost)
        implements CardEffect {

    public SeekDragonAndPerpetuallyBoostEffect() {
        this(1, 1);
    }
}
