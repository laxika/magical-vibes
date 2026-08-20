package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.Map;

/**
 * Death trigger for "When this creature dies / whenever an ally creature dies, if it had counters
 * on it, put those counters on up to one target creature" (e.g. Scolding Administrator and Host of
 * the Hereafter).
 * <p>
 * On {@code ON_DEATH}, the death-trigger collector snapshots the dying permanent's counters into
 * {@code counters}; if the map is empty the trigger does not fire. On
 * {@code ON_ALLY_CREATURE_DIES}, the ally-death pipeline binds the same snapshot through
 * {@link DyingCreatureCountersAwareEffect}. Resolution places each counter on the chosen creature.
 *
 * @param counters snapshot of the dying creature's counters, keyed by type (empty on the marker
 *                 instance placed on the card; filled in by the collector)
 */
public record MoveDyingSourceCountersToTargetCreatureEffect(Map<CounterType, Integer> counters)
        implements CardEffect, DyingCreatureCountersAwareEffect {

    public MoveDyingSourceCountersToTargetCreatureEffect {
        counters = Map.copyOf(counters);
    }

    public MoveDyingSourceCountersToTargetCreatureEffect() {
        this(Map.of());
    }

    @Override
    public CardEffect boundToDyingCreatureCounters(Map<CounterType, Integer> counters) {
        return new MoveDyingSourceCountersToTargetCreatureEffect(counters);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), new PermanentIsCreaturePredicate());
    }
}
