package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * Additional cost that sacrifices one distinct permanent for each filter in {@code filters}, in
 * order — e.g. Angel's Herald's "Sacrifice a green creature, a white creature, and a blue creature".
 * Each pick must satisfy that slot's predicate, and a single permanent can only pay one slot
 * (once sacrificed it leaves the battlefield). {@code descriptions} holds a human-readable label per
 * slot (parallel to {@code filters}) for the choice prompt.
 *
 * <p>Distinct from listing multiple {@link SacrificePermanentCost} costs on one ability: those are
 * separate cost effects and the activation resume path only carries a single cost effect through
 * interactive picks, so a per-slot sequence must be a single cost. Paid one choice at a time via
 * the {@code chosenSoFar} machinery (like {@link TapTwoCreaturesSharingTypeCost}); a slot is only
 * offered permanents whose selection still leaves a complete matching for the remaining slots.
 */
public record SacrificePermanentsSequenceCost(List<PermanentPredicate> filters, List<String> descriptions)
        implements CostEffect {

    public SacrificePermanentsSequenceCost {
        filters = List.copyOf(filters);
        descriptions = List.copyOf(descriptions);
        if (filters.size() != descriptions.size()) {
            throw new IllegalArgumentException("filters and descriptions must be parallel");
        }
    }
}
