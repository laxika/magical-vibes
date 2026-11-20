package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The resolution scratchpad of one {@link StackEntry}: what an earlier step of the resolution left for
 * a later step to read. A step is one effect in the entry's effect list, and the steps run in the order
 * written (CR 608.2c), so "sacrifice this; if you do, draw a card" is a sacrifice step that records its
 * {@link DepartureResult} followed by a condition step that reads it.
 *
 * <p>Each step of a {@link StepKind} records its {@link StepOutcome} ({@link #withOutcome}), replacing the
 * outcome of the previous step of that kind, even when nothing happened, so a later step never reads an older
 * step's result. A later step reads it through {@link #outcome}: the {@code HappenedThisWay} condition, the
 * {@code AmountThisWay} amount and the {@code thisWay} / {@code playersThisWay} subjects. A removal step's
 * {@link DepartureResult}s are its {@link StepKind#DESTROY} and {@link StepKind#SACRIFICE} outcomes too.</p>
 *
 * <p>Immutable, so a {@link StackEntry} copy shares it safely with the live game.</p>
 *
 * @param departures the departures recorded by the most recent step that removed permanents ("this
 *                   way"), including those that stayed on the battlefield (indestructible,
 *                   regenerated), with their last-known information (CR 608.2h). A removal step records
 *                   its results even when nothing left, so a later step never reads an older step's departures
 * @param pickedIds  the ids a player picked in an earlier step, for the next step to act on
 * @param outcomes   the outcome of the most recent step of each kind
 */
public record ResolutionResults(List<DepartureResult> departures, List<UUID> pickedIds,
                                Map<StepKind, StepOutcome> outcomes) {

    public static final ResolutionResults EMPTY = new ResolutionResults(List.of(), List.of(), Map.of());

    public ResolutionResults {
        departures = List.copyOf(departures);
        pickedIds = List.copyOf(pickedIds);
        outcomes = Collections.unmodifiableMap(outcomes.isEmpty()
                ? new EnumMap<>(StepKind.class) : new EnumMap<>(outcomes));
    }

    /**
     * These results with {@code results} as the latest step's departures, replacing the earlier ones; the
     * permanents among them that were destroyed and those that were sacrificed (wherever they ended up) are the
     * step's {@link StepKind#DESTROY} and {@link StepKind#SACRIFICE} outcomes.
     */
    public ResolutionResults withDepartures(List<DepartureResult> results) {
        Map<StepKind, StepOutcome> updated = new EnumMap<>(StepKind.class);
        updated.putAll(outcomes);
        updated.put(StepKind.DESTROY, departed(results, true));
        updated.put(StepKind.SACRIFICE, departed(results, false));
        return new ResolutionResults(results, pickedIds, updated);
    }

    /** These results with {@code ids} as the latest picks, replacing the earlier ones. */
    public ResolutionResults withPickedIds(List<UUID> ids) {
        return new ResolutionResults(departures, ids, outcomes);
    }

    /** These results with {@code outcome} as the latest {@code kind} step's outcome, replacing the earlier one. */
    public ResolutionResults withOutcome(StepKind kind, StepOutcome outcome) {
        Map<StepKind, StepOutcome> updated = new EnumMap<>(StepKind.class);
        updated.putAll(outcomes);
        updated.put(kind, outcome);
        return new ResolutionResults(departures, pickedIds, updated);
    }

    /** The outcome of the most recent {@code kind} step, or {@link StepOutcome#NONE} when none ran. */
    public StepOutcome outcome(StepKind kind) {
        return outcomes.getOrDefault(kind, StepOutcome.NONE);
    }

    /** Whether a permanent was sacrificed this way, wherever it ended up. */
    public boolean anySacrificed() {
        return departures.stream().anyMatch(DepartureResult::sacrificed);
    }

    /** Whether a permanent was destroyed this way, wherever it ended up. */
    public boolean anyDestroyed() {
        return departures.stream().anyMatch(DepartureResult::destroyed);
    }

    /**
     * The total last-known power of the permanents removed this way (CR 608.2h), each counted as at
     * least 0. A permanent that wasn't a creature counts 0.
     */
    public int lastKnownPower() {
        int total = 0;
        for (DepartureResult result : departures) {
            total = SaturatingMath.add(total, nonNegative(result.lastKnownPower()));
        }
        return total;
    }

    /** The total last-known toughness of the permanents removed this way; see {@link #lastKnownPower}. */
    public int lastKnownToughness() {
        int total = 0;
        for (DepartureResult result : departures) {
            total = SaturatingMath.add(total, nonNegative(result.lastKnownToughness()));
        }
        return total;
    }

    private static StepOutcome departed(List<DepartureResult> results, boolean destroyed) {
        List<UUID> permanents = new ArrayList<>();
        List<UUID> controllers = new ArrayList<>();
        for (DepartureResult result : results) {
            if (destroyed ? result.destroyed() : result.sacrificed()) {
                permanents.add(result.permanentId());
                if (result.lastKnownControllerId() != null) {
                    controllers.add(result.lastKnownControllerId());
                }
            }
        }
        return StepOutcome.ofObjects(permanents, controllers);
    }

    private static int nonNegative(Integer value) {
        return value == null ? 0 : Math.max(0, value);
    }
}
