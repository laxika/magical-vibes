package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * One-shot effect: the resolving controller may cast spells of the given type this turn as though
 * they had flash. The permission lasts until end of turn and applies to every matching spell.
 */
public record GrantFlashToCardTypeThisTurnEffect(CardPredicate filter) implements CardEffect {

    public GrantFlashToCardTypeThisTurnEffect(CardType cardType) {
        this(new CardTypePredicate(cardType));
    }
}
