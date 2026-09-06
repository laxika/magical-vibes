package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandCost;
import com.github.laxika.magicalvibes.model.effect.PutTimeCountersOnImprintedCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "157")
public class JhoiraOfTheGhitu extends Card {

    public JhoiraOfTheGhitu() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new ExileCardFromHandCost(
                                new CardNotPredicate(new CardTypePredicate(CardType.LAND)),
                                "nonland",
                                1,
                                true),
                        new PutTimeCountersOnImprintedCardEffect(4)
                ),
                "{2}, Exile a nonland card from your hand: Put four time counters on the exiled card. "
                        + "If it doesn't have suspend, it gains suspend."
        ));
    }
}
