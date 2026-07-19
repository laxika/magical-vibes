package com.github.laxika.magicalvibes.model.effect;

/**
 * Discards the controller's entire hand, then each opponent sacrifices a creature of their choice
 * for each card discarded this way (the sacrifice count equals the number of cards discarded).
 * Used by Malfegor.
 */
public record DiscardOwnHandThenEachOpponentSacrificesCreaturePerCardEffect() implements CardEffect {
}
