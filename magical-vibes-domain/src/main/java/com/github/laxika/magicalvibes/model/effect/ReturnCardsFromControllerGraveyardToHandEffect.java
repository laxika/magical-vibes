package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Returns up to {@code maxCount} matching cards from the controller's graveyard to their hand.
 * The controller chooses cards one at a time and may stop before reaching {@code maxCount}.
 *
 * @param filter the graveyard cards that may be returned
 * @param maxCount the maximum number of cards to return
 */
public record ReturnCardsFromControllerGraveyardToHandEffect(
        CardPredicate filter,
        DynamicAmount maxCount
) implements CardEffect {
}
