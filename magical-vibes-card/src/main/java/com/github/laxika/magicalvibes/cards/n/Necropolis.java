package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.ImprintedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "DRK", collectorNumber = "105")
public class Necropolis extends Card {

    public Necropolis() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(
                        new ExileCardFromGraveyardCost(CardType.CREATURE, false, true),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ZERO_PLUS_ONE, new ImprintedCardManaValue())
                ),
                "Exile a creature card from your graveyard: Put X +0/+1 counters on this creature, where X is the exiled card's mana value."
        ));
    }
}
