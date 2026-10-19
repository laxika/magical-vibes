package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * "For each [type], return up to one card of that type from your graveyard to the battlefield":
 * nothing is targeted. As the effect resolves, the controller picks up to one card for each filter in
 * order (a filter with no matching card is skipped, and one card may be picked for several filters),
 * then every picked card enters the battlefield simultaneously. The number of cards that actually
 * entered is stored as the resolving entry's event value for a following "for each card returned
 * this way" effect (Revival Experiment).
 *
 * @param filters           one card predicate per choice, in the order the choices are made
 * @param descriptions      human-readable name of each choice (e.g. "artifact card")
 */
public record ReturnOneCardPerFilterFromGraveyardOnResolutionEffect(
        List<CardPredicate> filters,
        List<String> descriptions
) implements CardEffect {

    public ReturnOneCardPerFilterFromGraveyardOnResolutionEffect {
        if (filters == null || filters.isEmpty() || descriptions == null || filters.size() != descriptions.size()) {
            throw new IllegalArgumentException("Filters and descriptions must have equal non-zero sizes");
        }
        filters = List.copyOf(filters);
        descriptions = List.copyOf(descriptions);
    }
}
