package com.github.laxika.magicalvibes.model.effect;

/**
 * Static draw-replacement capability: while a permanent carrying this effect is on the battlefield,
 * if its controller would draw a card, instead they reveal the top card of their library. If that
 * card is a creature card, put it into their graveyard; otherwise, they draw a card (the revealed
 * one). The draw is fully replaced when the library is empty — nothing is revealed and the player
 * does not lose. Detected in {@code DrawService.resolveDrawCard} via this interface (read as a fact,
 * never {@code instanceof}-ed on a concrete type). Enduring Renewal.
 */
public interface RevealTopCreatureToGraveyardElseDrawReplacementEffect extends CardEffect {
}
