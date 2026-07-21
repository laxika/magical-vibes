package com.github.laxika.magicalvibes.model.effect;

/**
 * Draws a card, then forces the controller to discard a card. The source permanent then gets
 * +X/+0 until end of turn, where X is the discarded card's mana value.
 * <p>
 * Used by Spellbound Dragon (ARB): "Whenever this creature attacks, draw a card, then discard a
 * card. This creature gets +X/+0 until end of turn, where X is the discarded card's mana value."
 */
public record DrawDiscardBoostSelfByDiscardedManaValueEffect() implements CardEffect {
}
