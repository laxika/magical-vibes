package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

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
 */
public record PutCounterOnTargetForEachDyingSourceCounterEffect(CounterType counterType, int count) implements CardEffect {

    public PutCounterOnTargetForEachDyingSourceCounterEffect(CounterType counterType) {
        this(counterType, 0);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }

    @Override
    public PermanentPredicate targetPredicate() {
        return new PermanentIsCreaturePredicate();
    }
}
