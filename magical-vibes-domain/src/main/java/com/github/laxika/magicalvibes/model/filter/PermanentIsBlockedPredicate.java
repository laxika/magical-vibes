package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a blocked creature: an attacking creature that at least one creature is blocking
 * (i.e. some permanent references its id as a blocking target). Distinct from
 * {@link PermanentIsBlockingPredicate}, which matches the blockers themselves.
 */
public record PermanentIsBlockedPredicate() implements PermanentPredicate {
}
