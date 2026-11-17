package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses another player, then that player gains permanent control of the target
 * permanent.
 */
public record ChooseAnotherPlayerGainsControlOfTargetPermanentEffect() implements ControlStealingEffect {

    @Override
    public Duration controlDuration() {
        return Duration.PERMANENT;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
