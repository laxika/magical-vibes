package com.github.laxika.magicalvibes.model.effect;

/**
 * Look at the top N cards of your library. Put one of them into your hand
 * and the rest into your graveyard.
 * Used by Forbidden Alchemy, Strategic Planning, etc.
 *
 * @param count number of cards to look at
 */
public record LookAtTopCardsChooseOneToHandRestToGraveyardEffect(int count) implements CardEffect {
}
