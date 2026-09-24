package com.github.laxika.magicalvibes.model.effect;

/** A randomly chosen opponent gains permanent control of the source permanent. */
public record RandomOpponentGainsControlOfSourceEffect() implements ControlStealingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    @Override
    public boolean targetChosenAtRandom() {
        return true;
    }

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }
}
