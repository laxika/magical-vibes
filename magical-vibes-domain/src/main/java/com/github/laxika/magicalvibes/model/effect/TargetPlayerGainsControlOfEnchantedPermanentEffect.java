package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives the target player control of the permanent attached to the source Aura or Equipment for
 * the configured duration. Set {@code targetsPlayer} to false when the recipient is supplied by
 * the triggering event rather than chosen as a target.
 */
public record TargetPlayerGainsControlOfEnchantedPermanentEffect(ControlDuration duration, boolean targetsPlayer)
        implements ControlStealingEffect {

    public TargetPlayerGainsControlOfEnchantedPermanentEffect(ControlDuration duration) {
        this(duration, true);
    }

    public TargetPlayerGainsControlOfEnchantedPermanentEffect() {
        this(ControlDuration.PERMANENT);
    }

    @Override
    public ControlDuration controlDuration() {
        return duration;
    }

    @Override
    public TargetSpec targetSpec() {
        return targetsPlayer ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
