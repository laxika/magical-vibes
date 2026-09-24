package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The greatest number of battlefield permanents matching {@code filter} controlled by any one
 * opponent of the amount's controller.
 */
public record GreatestPermanentCountAmongOpponents(PermanentPredicate filter) implements DynamicAmount {
}
