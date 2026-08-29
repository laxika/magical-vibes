package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "153")
public class Solarion extends Card {

    public Solarion() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PutCountersOnSelfEffect(
                        CounterType.PLUS_ONE_PLUS_ONE,
                        new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE))),
                "{T}: Double the number of +1/+1 counters on this creature."));
    }
}
