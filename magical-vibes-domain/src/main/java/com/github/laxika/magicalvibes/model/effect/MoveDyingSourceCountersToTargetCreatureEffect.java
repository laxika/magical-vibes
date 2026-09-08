package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;

/**
 * Death trigger for transferring every concrete counter from a dying creature to a target creature
 * (e.g. Scolding Administrator, Host of the Hereafter, and Parish-Blade Trainee).
 * <p>
 * On {@code ON_DEATH}, the death-trigger collector snapshots the dying permanent's counters into
 * {@code counters}. When {@code requiresCounters} is true, an empty snapshot suppresses the
 * trigger; otherwise the trigger still requires a target and simply places no counters. On
 * {@code ON_ALLY_CREATURE_DIES}, the ally-death pipeline binds the same snapshot through
 * {@link DyingCreatureCountersAwareEffect}. Resolution places each counter on the chosen creature.
 *
 * @param counters snapshot of the dying creature's counters, keyed by type (empty on the marker
 *                 instance placed on the card; filled in by the collector)
 * @param controllerOnly whether the target must be a creature controlled by the trigger controller
 * @param requiresCounters whether an empty counter snapshot suppresses the death trigger
 */
public record MoveDyingSourceCountersToTargetCreatureEffect(
        Map<CounterType, Integer> counters,
        boolean controllerOnly,
        boolean requiresCounters
)
        implements CardEffect, DyingCreatureCountersAwareEffect {

    public MoveDyingSourceCountersToTargetCreatureEffect {
        counters = Map.copyOf(counters);
    }

    public MoveDyingSourceCountersToTargetCreatureEffect() {
        this(Map.of(), false, true);
    }

    public MoveDyingSourceCountersToTargetCreatureEffect(Map<CounterType, Integer> counters) {
        this(counters, false, true);
    }

    public MoveDyingSourceCountersToTargetCreatureEffect(boolean controllerOnly) {
        this(Map.of(), controllerOnly, true);
    }

    /** Creates the mandatory-target form that also triggers when the source had no counters. */
    public static MoveDyingSourceCountersToTargetCreatureEffect alwaysTriggers(boolean controllerOnly) {
        return new MoveDyingSourceCountersToTargetCreatureEffect(Map.of(), controllerOnly, false);
    }

    @Override
    public CardEffect boundToDyingCreatureCounters(Map<CounterType, Integer> counters) {
        return new MoveDyingSourceCountersToTargetCreatureEffect(counters, controllerOnly, requiresCounters);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(), controllerOnly
                ? new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate()))
                : new PermanentIsCreaturePredicate());
    }
}
