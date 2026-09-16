package com.github.laxika.magicalvibes.model.effect;

/**
 * Gains control of the target creature until the source leaves the battlefield or that
 * creature's controller pays the configured ransom.
 */
public record GainControlOfTargetUntilRansomEffect(int manaCost, int lifeCost)
        implements ControlStealingEffect {

    public GainControlOfTargetUntilRansomEffect {
        if (manaCost < 0 || lifeCost < 0) {
            throw new IllegalArgumentException("Ransom costs cannot be negative");
        }
    }

    @Override
    public ControlDuration controlDuration() {
        return ControlDuration.WHILE_SOURCE_REMAINS;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
