package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Exchanges a matching card from the controller's outside-game pool with a card in hand. */
public record ExchangeCardFromOutsideGameWithHandEffect(CardPredicate filter) implements CardEffect {
}
