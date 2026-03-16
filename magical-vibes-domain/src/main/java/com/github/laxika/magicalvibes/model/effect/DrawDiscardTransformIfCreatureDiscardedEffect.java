package com.github.laxika.magicalvibes.model.effect;

/**
 * Draws a card, then forces the controller to discard a card.
 * If the discarded card is a creature card, untaps and transforms the source permanent.
 * <p>
 * Used by Civilized Scholar (ISD): "{T}: Draw a card, then discard a card.
 * If a creature card is discarded this way, untap Civilized Scholar, then transform it."
 */
public record DrawDiscardTransformIfCreatureDiscardedEffect() implements CardEffect {
}
