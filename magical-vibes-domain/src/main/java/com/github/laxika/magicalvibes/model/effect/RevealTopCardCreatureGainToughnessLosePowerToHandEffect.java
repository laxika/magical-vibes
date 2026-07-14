package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveal the top card of the controller's library. If it's a creature card, the controller gains
 * life equal to that card's toughness, loses life equal to its power, then puts it into their hand.
 * If it isn't a creature card, it simply stays revealed on top of the library.
 *
 * <p>Used by Sapling of Colfenor.
 */
public record RevealTopCardCreatureGainToughnessLosePowerToHandEffect() implements CardEffect {
}
