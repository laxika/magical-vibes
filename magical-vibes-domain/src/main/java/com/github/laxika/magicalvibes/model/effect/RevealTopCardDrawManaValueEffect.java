package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveal the top card of your library. Draw cards equal to that card's mana value.
 * The revealed card stays on top of the library, so it is the first card drawn when the
 * mana value is greater than zero.
 */
public record RevealTopCardDrawManaValueEffect() implements CardEffect {
}
