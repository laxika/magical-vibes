package com.github.laxika.magicalvibes.model.effect;

/**
 * Static draw-replacement effect: if an opponent of the source's controller would draw a card
 * except the first one they draw in each of their draw steps, that player skips the draw and the
 * controller creates a Treasure token instead (Hullbreacher).
 */
public record OpponentExtraDrawsCreateTreasureEffect() implements CardEffect {
}
