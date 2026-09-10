package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** The number of distinct names among permanents in the selected player scope. */
public record DistinctPermanentNamesAmongControlled(PermanentPredicate filter, CountScope scope)
        implements DynamicAmount {
}
