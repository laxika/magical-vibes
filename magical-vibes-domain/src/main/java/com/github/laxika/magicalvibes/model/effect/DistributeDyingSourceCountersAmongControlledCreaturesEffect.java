package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * Death trigger for "When this creature dies, you may distribute a number of {@code counterType}
 * counters equal to the number of {@code counterType} counters on this creature among any number of
 * creatures you control" (Vastwood Hydra).
 * <p>
 * Placed on the {@code ON_DEATH} slot. The death-trigger collector snapshots the dying permanent's
 * count of {@code counterType} into {@code count}, wraps the baked effect in a {@link MayEffect},
 * and puts it on the stack. The ability does not target (Gatherer) — division is chosen when it
 * resolves via {@code GameData.pendingETBDamageAssignments}, the same buffer used by Inferno Titan /
 * Gang of Devils divided damage. Only creatures the trigger's controller controls receive counters.
 *
 * @param counterType the counter type counted on the dying creature and placed on controlled creatures
 * @param count       snapshot of the dying creature's counter count (0 on the marker instance placed
 *                    on the card; filled in by the collector)
 */
public record DistributeDyingSourceCountersAmongControlledCreaturesEffect(
        CounterType counterType, int count) implements CardEffect {

    public DistributeDyingSourceCountersAmongControlledCreaturesEffect(CounterType counterType) {
        this(counterType, 0);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
