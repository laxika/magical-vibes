package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a permanent when another permanent controlled by its current controller has the same
 * name.
 */
public record PermanentSharesNameWithAnotherControlledPermanentPredicate() implements PermanentPredicate {
}
