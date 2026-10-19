package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches abilities whose source permanent is the evaluating source permanent, including copies of
 * such abilities. Used by "copy target ability from another source" filters that must not pick an
 * ability of the same permanent. Source-dependent: matches nothing unless the source permanent is
 * passed to {@code TargetLegalityService.matchesStackEntryPredicate(..., source)}; the ability-activation
 * path supplies it automatically.
 */
public record StackEntryIsFromSourcePermanentPredicate() implements StackEntryPredicate {
}
