package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives the target player control of the permanent attached to the source Aura or Equipment for
 * the configured duration.
 */
public record TargetPlayerGainsControlOfEnchantedPermanentEffect(ControlDuration duration)
        implements ControlStealingEffect {

    public TargetPlayerGainsControlOfEnchantedPermanentEffect() {
        this(ControlDuration.PERMANENT);
    }

    @Override
    public ControlDuration controlDuration() {
        return duration;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
