package com.github.laxika.magicalvibes.model.effect;

/**
 * Look at the top N cards of your library. Put some into your hand
 * and the rest into your graveyard.
 * Used by Forbidden Alchemy (4, 1), Dark Bargain (3, 2), etc.
 *
 * @param count       number of cards to look at
 * @param toHandCount number of cards to put into hand (rest go to graveyard)
 */
public record LookAtTopCardsChooseNToHandRestToGraveyardEffect(int count, int toHandCount) implements CardEffect {
}
