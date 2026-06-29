package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Look at or reveal the top N cards of your library. Put some into your hand
 * and the rest into your graveyard.
 * Used by Forbidden Alchemy (4, 1), Dark Bargain (3, 2), Tracker's Instincts (4, 1, creature, reveal), etc.
 *
 * @param count               number of cards to look at or reveal
 * @param toHandCount         number of cards to put into hand (rest go to graveyard)
 * @param handChoicePredicate when non-null, only matching cards may be chosen for hand
 * @param reveal              when true, cards are revealed to all players instead of looked at privately
 */
public record LookAtTopCardsChooseNToHandRestToGraveyardEffect(
        int count,
        int toHandCount,
        CardPredicate handChoicePredicate,
        boolean reveal
) implements CardEffect {

    public LookAtTopCardsChooseNToHandRestToGraveyardEffect(int count, int toHandCount) {
        this(count, toHandCount, null, false);
    }
}
