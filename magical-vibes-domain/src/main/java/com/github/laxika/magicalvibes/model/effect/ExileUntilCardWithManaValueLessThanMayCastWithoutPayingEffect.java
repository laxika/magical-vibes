package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles cards from the controller's library until a card matching {@code predicate} with mana
 * value strictly less than {@code manaValue} is found. The other exiled cards are put on the
 * bottom of the library in a random order, and the found card is offered for a free cast.
 */
public record ExileUntilCardWithManaValueLessThanMayCastWithoutPayingEffect(
        CardPredicate predicate,
        DynamicAmount manaValue
) implements CardEffect {
}
