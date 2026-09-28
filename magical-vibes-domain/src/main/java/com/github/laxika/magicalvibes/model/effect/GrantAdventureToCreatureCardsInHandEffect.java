package com.github.laxika.magicalvibes.model.effect;

/**
 * Static permission marker for granting a synthetic Adventure face to creature cards in hand.
 * The hand-characteristics service supplies the Adventure face while preserving the physical card.
 */
public record GrantAdventureToCreatureCardsInHandEffect() implements CardEffect {
}
