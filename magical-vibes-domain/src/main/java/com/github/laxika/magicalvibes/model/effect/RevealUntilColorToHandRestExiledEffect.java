package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Reveals cards from the top of the controller's library until a card of the given color is
 * revealed. That card is put into the controller's hand, and all other cards revealed this way are
 * exiled. If the library is exhausted without revealing a card of that color, every revealed card
 * is exiled.
 * <p>
 * Used by Sacred Guide ({@link CardColor#WHITE}).
 */
public record RevealUntilColorToHandRestExiledEffect(CardColor color) implements CardEffect {
}
