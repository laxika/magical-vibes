package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "215")
public class AngelheartVial extends Card {

    public AngelheartVial() {
        addTrigger(Triggers.youAreDealtDamage(),
                new MayEffect(
                        new PutCountersOnSelfEffect(CounterType.CHARGE, new EventValue()),
                        "Put that many charge counters on Angelheart Vial?"));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new RemoveCounterFromSourceCost(4, CounterType.CHARGE),
                        new GainLifeEffect(2),
                        new DrawCardEffect(1)
                ),
                "{2}, {T}, Remove four charge counters from Angelheart Vial: You gain 2 life and draw a card."
        ));
    }
}
