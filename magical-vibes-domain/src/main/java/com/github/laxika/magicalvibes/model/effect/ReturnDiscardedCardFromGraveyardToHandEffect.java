package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns the exact card carried by a discard trigger from its owner's graveyard to that
 * player's hand. Does nothing if the trigger has no card or the card has left the graveyard.
 */
public record ReturnDiscardedCardFromGraveyardToHandEffect() implements CardEffect {
}
