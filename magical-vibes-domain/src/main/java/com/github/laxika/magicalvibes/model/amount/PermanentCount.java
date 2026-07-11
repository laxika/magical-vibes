package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The number of battlefield permanents matching the predicate within the given scope.
 * {@code excludeSource} models "each <em>other</em> …" wordings — the source permanent
 * itself is never counted even when it matches.
 */
public record PermanentCount(PermanentPredicate filter, CountScope scope,
                             boolean excludeSource) implements DynamicAmount {

    public PermanentCount(PermanentPredicate filter, CountScope scope) {
        this(filter, scope, false);
    }
}
