package com.github.laxika.magicalvibes.model.effect;

/**
 * Starting with the effect controller, each player votes for grace or condemnation. A grace
 * majority returns each player's creature cards from their graveyard to the battlefield; otherwise
 * all creatures other than the source creature are destroyed.
 */
public record GraceOrCondemnationEffect() implements CardEffect {
}
