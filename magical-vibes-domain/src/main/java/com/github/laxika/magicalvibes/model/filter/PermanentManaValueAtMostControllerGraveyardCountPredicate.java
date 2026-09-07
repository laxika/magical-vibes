package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents whose mana value is at most the number of non-token cards in that
 * permanent's controller's graveyard.
 */
public record PermanentManaValueAtMostControllerGraveyardCountPredicate() implements PermanentPredicate {
}
