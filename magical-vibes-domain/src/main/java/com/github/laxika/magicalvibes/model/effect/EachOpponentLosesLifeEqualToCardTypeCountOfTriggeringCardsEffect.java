package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

import java.util.List;

/** Each opponent loses life equal to the number of distinct card types among triggering cards. */
public record EachOpponentLosesLifeEqualToCardTypeCountOfTriggeringCardsEffect(List<Card> triggeringCards)
        implements CardEffect, TriggeringCardsAwareEffect {

    public EachOpponentLosesLifeEqualToCardTypeCountOfTriggeringCardsEffect() {
        this(List.of());
    }

    public EachOpponentLosesLifeEqualToCardTypeCountOfTriggeringCardsEffect {
        triggeringCards = List.copyOf(triggeringCards);
    }

    @Override
    public CardEffect withTriggeringCards(List<Card> cards) {
        return new EachOpponentLosesLifeEqualToCardTypeCountOfTriggeringCardsEffect(cards);
    }
}
