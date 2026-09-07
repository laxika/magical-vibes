package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "135")
@CardRegistration(set = "TPR", collectorNumber = "227")
@CardRegistration(set = "TSB", collectorNumber = "111")
public class MindlessAutomaton extends Card {

    public MindlessAutomaton() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(2)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new PutCountersOnSourceEffect(1, 1, 1)
                ),
                "{1}, Discard a card: Put a +1/+1 counter on this creature."
        ));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new RemoveCounterFromSourceCost(2, CounterType.PLUS_ONE_PLUS_ONE),
                        new DrawCardEffect(1)
                ),
                "Remove two +1/+1 counters from this creature: Draw a card."
        ));
    }
}
