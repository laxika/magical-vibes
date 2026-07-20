package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

/**
 * Death trigger for "When this creature dies, put a {@code counterType} counter on target creature
 * for each {@code counterType} counter on this creature" (e.g. Grief Tyrant, which enters with four
 * -1/-1 counters and hands them out on death).
 * <p>
 * Placed on the {@code ON_DEATH} slot. The death-trigger collector snapshots the dying permanent's
 * count of {@code counterType} at the moment of death into {@code count} (the source is off the
 * battlefield by the time this resolves), queues a targeted death trigger, and resolution places
 * {@code count} counters of {@code counterType} on the chosen creature.
 *
 * @param counterType the counter type counted on the dying creature and placed on the target
 * @param count       snapshot of the dying creature's counter count (0 on the marker instance placed
 *                    on the card; filled in by the collector)
 * @param optional    when {@code true} this is a "you may" death trigger (Soulstinger) — the target
 *                    is still chosen as the trigger goes on the stack (CR 603.3d), but the controller
 *                    may decline placing the counters at resolution; the collector wraps the
 *                    snapshotted effect in a {@code MayEffect}
 */
public record PutCounterOnTargetForEachDyingSourceCounterEffect(CounterType counterType, int count, boolean optional) implements CardEffect {

    public PutCounterOnTargetForEachDyingSourceCounterEffect(CounterType counterType) {
        this(counterType, 0, false);
    }

    public PutCounterOnTargetForEachDyingSourceCounterEffect(CounterType counterType, boolean optional) {
        this(counterType, 0, optional);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT, new PermanentIsCreaturePredicate());
    }
}
