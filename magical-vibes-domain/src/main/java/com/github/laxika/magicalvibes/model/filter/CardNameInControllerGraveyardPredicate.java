package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches a card whose name is present in the perspective player's graveyard.
 * The perspective player is the {@code cardOwnerId} supplied to card-predicate evaluation.
 */
public record CardNameInControllerGraveyardPredicate() implements CardPredicate {
}
