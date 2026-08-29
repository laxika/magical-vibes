package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent that has at least one Equipment attached to it.
 * Requires game data to evaluate because the battlefield must be scanned for attachments.
 */
public record PermanentIsEquippedPredicate() implements PermanentPredicate {
}
