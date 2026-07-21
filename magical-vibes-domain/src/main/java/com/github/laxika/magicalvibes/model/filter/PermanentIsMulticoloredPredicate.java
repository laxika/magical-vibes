package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent that is multicolored, i.e. has two or more colours among its effective
 * colours ({@code Card.getColors()} as modified by the CR 613 layer system). Colourless
 * permanents (zero colours) and monocoloured permanents (exactly one colour) do not match.
 * The battlefield counterpart of {@link CardIsMulticoloredPredicate} and the complement of
 * {@link PermanentIsMonocoloredPredicate}. Used by "another multicolored permanent" filters
 * (Esper Stormblade).
 */
public record PermanentIsMulticoloredPredicate() implements PermanentPredicate {
}
