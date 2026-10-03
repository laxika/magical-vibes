package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "186")
@CardRegistration(set = "C21", collectorNumber = "137")
@CardRegistration(set = "C18", collectorNumber = "14")
@CardRegistration(set = "VOC", collectorNumber = "122")
public class Bloodtracker extends Card {

    public Bloodtracker() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new PayLifeCost(2),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)
                ),
                "{B}, Pay 2 life: Put a +1/+1 counter on this creature."
        ));

        addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new DrawCardEffect(new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
