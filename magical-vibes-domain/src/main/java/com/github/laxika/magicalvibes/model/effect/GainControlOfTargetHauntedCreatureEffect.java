package com.github.laxika.magicalvibes.model.effect;

/** Gains control of target creature indefinitely while it remains haunted. */
public record GainControlOfTargetHauntedCreatureEffect() implements ControlStealingEffect {

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.PERMANENT;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
