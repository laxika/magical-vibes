package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose mana value is at most the number of cards in the source controller's
 * hand. Evaluation uses the current hand size at resolution time.
 */
public record PermanentManaValueAtMostSourceControllerHandSizePredicate() implements PermanentPredicate {
}
