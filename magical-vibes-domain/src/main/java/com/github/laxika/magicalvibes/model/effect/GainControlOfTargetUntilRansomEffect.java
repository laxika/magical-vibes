package com.github.laxika.magicalvibes.model.effect;

/**
 * Gains control of the target creature until the source leaves the battlefield or the player it was taken from pays
 * the configured ransom. "Until it leaves" doesn't end as the source phases out, which isn't leaving (CR 702.26d).
 */
public record GainControlOfTargetUntilRansomEffect(int manaCost, int lifeCost)
        implements ControlStealingEffect {

    public GainControlOfTargetUntilRansomEffect {
        if (manaCost < 0 || lifeCost < 0) {
            throw new IllegalArgumentException("Ransom costs cannot be negative");
        }
    }

    @Override
    public Duration controlDuration() {
        return Duration.UNTIL_SOURCE_LEAVES;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
