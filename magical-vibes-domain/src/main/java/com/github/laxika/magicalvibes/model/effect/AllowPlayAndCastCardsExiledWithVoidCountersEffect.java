package com.github.laxika.magicalvibes.model.effect;

/**
 * Static permission for the controller to play cards owned by opponents from exile while those
 * cards have void counters on them.
 */
public record AllowPlayAndCastCardsExiledWithVoidCountersEffect() implements CardEffect {
}
