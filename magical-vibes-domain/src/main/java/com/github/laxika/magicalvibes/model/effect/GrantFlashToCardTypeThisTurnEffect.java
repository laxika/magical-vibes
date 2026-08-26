package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * One-shot effect: the resolving controller may cast spells matching the filter as though they
 * had flash. The permission normally lasts until end of turn; {@link EffectDuration#UNTIL_YOUR_NEXT_TURN}
 * is also supported for effects such as Arlinn, the Pack's Hope.
 */
public record GrantFlashToCardTypeThisTurnEffect(CardPredicate filter, EffectDuration duration) implements CardEffect {

    public GrantFlashToCardTypeThisTurnEffect(CardPredicate filter) {
        this(filter, EffectDuration.UNTIL_END_OF_TURN);
    }

    public GrantFlashToCardTypeThisTurnEffect(CardType cardType) {
        this(new CardTypePredicate(cardType));
    }

    public GrantFlashToCardTypeThisTurnEffect(CardType cardType, EffectDuration duration) {
        this(new CardTypePredicate(cardType), duration);
    }
}
