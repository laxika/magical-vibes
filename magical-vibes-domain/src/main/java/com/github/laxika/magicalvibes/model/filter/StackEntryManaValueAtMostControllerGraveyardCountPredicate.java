package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches spells whose mana value is at most the number of non-token cards in the spell
 * controller's graveyard.
 */
public record StackEntryManaValueAtMostControllerGraveyardCountPredicate() implements StackEntryPredicate {
}
