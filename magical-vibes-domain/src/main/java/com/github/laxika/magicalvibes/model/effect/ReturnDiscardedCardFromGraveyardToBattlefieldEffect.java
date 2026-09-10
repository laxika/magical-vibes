package com.github.laxika.magicalvibes.model.effect;

/**
 * Returns the exact card carried by a discard follow-up from its owner's graveyard to the
 * battlefield. Does nothing if the trigger has no card or the card has left the graveyard.
 *
 * @param enterTapped whether the returned permanent enters tapped
 */
public record ReturnDiscardedCardFromGraveyardToBattlefieldEffect(boolean enterTapped)
        implements CardEffect {
}
