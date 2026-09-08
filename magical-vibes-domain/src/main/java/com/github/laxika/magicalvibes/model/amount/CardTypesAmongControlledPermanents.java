package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The number of distinct effective card types among battlefield permanents matching the filter
 * in the selected player scope. A multi-type permanent contributes each of its card types.
 */
public record CardTypesAmongControlledPermanents(
        PermanentPredicate filter,
        CountScope scope,
        boolean excludeSource
) implements DynamicAmount {

    public CardTypesAmongControlledPermanents(PermanentPredicate filter, CountScope scope) {
        this(filter, scope, false);
    }
}
