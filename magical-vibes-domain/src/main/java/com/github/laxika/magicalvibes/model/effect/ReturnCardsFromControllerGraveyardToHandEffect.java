package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Returns matching cards from the controller's graveyard to their hand. Optional returns let the
 * controller stop before reaching {@code maxCount}; mandatory returns move every matching card
 * automatically when no choice is needed.
 *
 * @param filter the graveyard cards that may be returned
 * @param maxCount the maximum number of cards to return
 * @param optional whether the controller may return fewer than the evaluated maximum
 */
public record ReturnCardsFromControllerGraveyardToHandEffect(
        CardPredicate filter,
        DynamicAmount maxCount,
        boolean optional
) implements CardEffect {

    public ReturnCardsFromControllerGraveyardToHandEffect(CardPredicate filter, DynamicAmount maxCount) {
        this(filter, maxCount, true);
    }
}
