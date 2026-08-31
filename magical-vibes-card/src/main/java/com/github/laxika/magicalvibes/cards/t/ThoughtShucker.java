package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "77")
public class ThoughtShucker extends Card {

    public ThoughtShucker() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{U}",
                List.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new DrawCardEffect(1)
                ),
                "Threshold — {1}{U}: Put a +1/+1 counter on this creature and draw a card. Activate only if there are seven or more cards in your graveyard and only once."
        ).withRequiredGraveyardCards(new CardTruePredicate(), 7, "cards in your graveyard")
                .withMaxActivationsPerGame(1));
    }
}
