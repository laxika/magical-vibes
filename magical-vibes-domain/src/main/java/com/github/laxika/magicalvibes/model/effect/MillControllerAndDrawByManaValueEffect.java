package com.github.laxika.magicalvibes.model.effect;

/**
 * "Mill a card, then draw cards equal to the milled card's mana value." (Heed the Mists.)
 * <p>
 * The controller mills one card; the draw count is that card's mana value. If the library is empty,
 * or a replacement effect kept the card out of the graveyard so it was never actually milled,
 * nothing is drawn.
 */
public record MillControllerAndDrawByManaValueEffect() implements CardEffect {
}
