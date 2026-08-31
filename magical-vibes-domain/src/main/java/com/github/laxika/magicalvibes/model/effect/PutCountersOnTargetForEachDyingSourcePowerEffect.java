package com.github.laxika.magicalvibes.model.effect;

/**
 * Death-trigger marker for putting that dying creature's last-known effective power as +1/+1
 * counters on up to one target creature.
 *
 * <p>The death-trigger collector snapshots the power before the creature leaves the battlefield
 * and stores it in {@link #count()} on the effect that is put on the stack.</p>
 *
 * @param count the snapshotted number of +1/+1 counters; zero on the card definition
 */
public record PutCountersOnTargetForEachDyingSourcePowerEffect(int count)
        implements CardEffect, OptionalTargetEffect {

    public PutCountersOnTargetForEachDyingSourcePowerEffect() {
        this(0);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
