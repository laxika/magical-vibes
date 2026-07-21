package com.github.laxika.magicalvibes.model.effect;

/**
 * Reveal the top card of your library. You gain life equal to that card's mana value.
 * The revealed card stays on top of the library.
 *
 * <p>Used by Sifter Wurm (after scry 3 on ETB).
 */
public record RevealTopCardGainLifeEqualToManaValueEffect() implements CardEffect {
}
