package com.github.laxika.magicalvibes.model.effect;

/**
 * Static permission for the controller to play lands and cast spells they own in exile that have
 * croak counters on them.
 */
public record AllowPlayAndCastCardsExiledWithCroakCountersEffect() implements CardEffect {
}
