package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The number of distinct names among permanents matching the filter in the selected player scope.
 */
public record DistinctPermanentNamesCount(PermanentPredicate filter, CountScope scope) implements DynamicAmount {
}
