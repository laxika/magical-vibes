package com.github.laxika.magicalvibes.model.effect;

/**
 * Offers to put one face-up card owned by an opponent from exile into that player's graveyard.
 * The resolving entry records whether a card was moved for a following conditional effect.
 */
public record PutOpponentOwnedExiledCardIntoGraveyardEffect() implements CardEffect {
}
