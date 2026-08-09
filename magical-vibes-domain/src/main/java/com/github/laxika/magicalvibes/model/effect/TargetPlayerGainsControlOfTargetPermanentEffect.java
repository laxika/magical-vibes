package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives the first target player permanent control of the second target permanent.
 */
public record TargetPlayerGainsControlOfTargetPermanentEffect() implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.playerOrPermanent());
    }
}
