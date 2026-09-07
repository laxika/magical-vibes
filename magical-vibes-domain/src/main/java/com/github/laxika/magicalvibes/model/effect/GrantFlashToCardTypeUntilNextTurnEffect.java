package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * One-shot effect: the resolving controller may cast matching spells as though they had flash
 * until their next turn.
 */
public record GrantFlashToCardTypeUntilNextTurnEffect(CardPredicate filter) implements CardEffect {

    public GrantFlashToCardTypeUntilNextTurnEffect(CardType cardType) {
        this(new CardTypePredicate(cardType));
    }
}
