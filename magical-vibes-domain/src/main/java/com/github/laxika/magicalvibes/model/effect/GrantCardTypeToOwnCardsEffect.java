package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/** Grants a card type to matching owned cards, optionally excluding the battlefield. */
public record GrantCardTypeToOwnCardsEffect(CardType cardType, CardPredicate filter, boolean includeBattlefield)
        implements OwnCardTypeGrantingEffect {
    public GrantCardTypeToOwnCardsEffect(CardType cardType, CardPredicate filter) {
        this(cardType, filter, true);
    }
}
