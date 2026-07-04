package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Map;

/**
 * Death trigger for "When this creature dies, if it had counters on it, put those counters on up to
 * one target creature" (e.g. Scolding Administrator).
 * <p>
 * Placed on the {@code ON_DEATH} slot. The death-trigger collector snapshots the dying permanent's
 * counters into {@code counters}; if the map is empty the trigger does not fire (the intervening-if
 * condition). When it fires, the collector queues a targeted death trigger carrying the snapshot, and
 * resolution places each counter on the chosen creature.
 *
 * @param counters snapshot of the dying creature's counters, keyed by type (empty on the marker
 *                 instance placed on the card; filled in by the collector)
 */
public record MoveDyingSourceCountersToTargetCreatureEffect(Map<CounterType, Integer> counters) implements CardEffect {

    public MoveDyingSourceCountersToTargetCreatureEffect() {
        this(Map.of());
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
