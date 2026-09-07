package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card whose mana value is at most the number of permanent cards in the perspective
 * player's graveyard.
 */
public record CardManaValueAtMostPermanentCardsInControllerGraveyardPredicate() implements CardPredicate {
}
