package com.github.laxika.magicalvibes.model.effect;

/**
 * Additional cast cost: discard a card or pay a fixed amount of life.
 */
public record DiscardCardOrPayLifeCost(int lifeAmount) implements CostEffect {
}
