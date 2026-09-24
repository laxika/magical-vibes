package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent that has an attached permanent matching {@link #predicate()}.
 */
public record PermanentHasAttachedPermanentPredicate(PermanentPredicate predicate)
        implements PermanentPredicate {
}
