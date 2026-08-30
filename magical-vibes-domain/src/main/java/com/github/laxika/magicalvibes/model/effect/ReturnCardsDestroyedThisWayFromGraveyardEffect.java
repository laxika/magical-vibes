package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns the cards from the controller's graveyard that were put there by the destruction event
 * recorded on the current stack entry.
 */
public record ReturnCardsDestroyedThisWayFromGraveyardEffect() implements CardEffect {
}
