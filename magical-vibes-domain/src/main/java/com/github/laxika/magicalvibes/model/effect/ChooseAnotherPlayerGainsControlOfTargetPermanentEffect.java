package com.github.laxika.magicalvibes.model.effect;

/**
 * The controller chooses another player, then that player gains permanent control of the target
 * permanent.
 */
public record ChooseAnotherPlayerGainsControlOfTargetPermanentEffect() implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
