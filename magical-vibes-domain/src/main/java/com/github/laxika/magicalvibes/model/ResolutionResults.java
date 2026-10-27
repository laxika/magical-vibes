package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

/**
 * The resolution scratchpad of one {@link StackEntry}: what an earlier step of the resolution left for
 * a later step to read. A step is one effect in the entry's effect list, and the steps run in the order
 * written (CR 608.2c), so "sacrifice this; if you do, draw a card" is a sacrifice step that records its
 * {@link DepartureResult} followed by a condition step that reads it.
 *
 * <p>Immutable, so a {@link StackEntry} copy shares it safely with the live game.</p>
 *
 * @param departures the departures recorded by the most recent step that removed permanents ("this
 *                   way"), including those that stayed on the battlefield (indestructible,
 *                   regenerated). A removal step records its results even when nothing left, so a later
 *                   step never reads an older step's departures
 * @param pickedIds  the ids a player picked in an earlier step, for the next step to act on
 * @param drawnCardIds the cards the most recent draw step actually drew ("this way"); a draw step records
 *                   its result even when nothing was drawn, so a later step never reads an older draw
 */
public record ResolutionResults(List<DepartureResult> departures, List<UUID> pickedIds, List<UUID> drawnCardIds) {

    public static final ResolutionResults EMPTY = new ResolutionResults(List.of(), List.of(), List.of());

    public ResolutionResults {
        departures = List.copyOf(departures);
        pickedIds = List.copyOf(pickedIds);
        drawnCardIds = List.copyOf(drawnCardIds);
    }

    /** These results with {@code results} as the latest step's departures, replacing the earlier ones. */
    public ResolutionResults withDepartures(List<DepartureResult> results) {
        return new ResolutionResults(results, pickedIds, drawnCardIds);
    }

    /** These results with {@code ids} as the latest picks, replacing the earlier ones. */
    public ResolutionResults withPickedIds(List<UUID> ids) {
        return new ResolutionResults(departures, ids, drawnCardIds);
    }

    /** These results with {@code ids} as the latest draw step's drawn cards, replacing the earlier ones. */
    public ResolutionResults withDrawnCardIds(List<UUID> ids) {
        return new ResolutionResults(departures, pickedIds, ids);
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

    private static int nonNegative(Integer value) {
        return value == null ? 0 : Math.max(0, value);
    }
}
