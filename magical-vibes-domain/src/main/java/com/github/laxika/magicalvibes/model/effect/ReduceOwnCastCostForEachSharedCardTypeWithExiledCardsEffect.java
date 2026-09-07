package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that reduces the generic casting cost of the source permanent's controller's
 * spells by one for each distinct card type they share with a card exiled with the source.
 */
public record ReduceOwnCastCostForEachSharedCardTypeWithExiledCardsEffect() implements CardEffect {
}
