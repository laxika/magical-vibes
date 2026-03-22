package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * A tap-untapped-permanent component of a casting cost.
 * Requires tapping {@code count} untapped permanents matching {@code filter}
 * (e.g. Zahid, Djinn of the Lamp: tap 1 untapped artifact you control).
 */
public record TapUntappedPermanentsCost(int count, PermanentPredicate filter) implements CastingCost {
}
