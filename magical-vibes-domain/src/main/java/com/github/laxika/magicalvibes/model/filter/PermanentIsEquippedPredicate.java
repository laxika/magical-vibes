package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent that has at least one Equipment attached to it.
 * Requires game data to evaluate because the battlefield is scanned for attached Equipment.
 */
public record PermanentIsEquippedPredicate() implements PermanentPredicate {
}
