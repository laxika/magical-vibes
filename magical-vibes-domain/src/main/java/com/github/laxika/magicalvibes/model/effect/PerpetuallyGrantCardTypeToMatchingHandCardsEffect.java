package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Perpetually grants a card type to every matching card currently in the controller's hand. */
public record PerpetuallyGrantCardTypeToMatchingHandCardsEffect(
        CardType cardType, CardPredicate filter) implements CardEffect {
}
