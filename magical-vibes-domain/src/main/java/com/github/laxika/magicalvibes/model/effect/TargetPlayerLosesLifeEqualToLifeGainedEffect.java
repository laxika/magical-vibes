package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker effect for "target opponent loses life equal to life gained".
 * At life-gain trigger time, {@code MiscTriggerCollectorService} reads the
 * life gained amount and replaces this with a concrete {@link TargetPlayerLosesLifeEffect}.
 */
public record TargetPlayerLosesLifeEqualToLifeGainedEffect() implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
