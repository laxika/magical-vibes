package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent currently attached to a creature. Requires game data to evaluate the
 * permanent that is being attached to.
 */
public record PermanentAttachedToCreaturePredicate() implements PermanentPredicate {
}
