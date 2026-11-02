package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndRemovePlusOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "JUD", collectorNumber = "140")
@CardRegistration(set = "DMR", collectorNumber = "193")
@CardRegistration(set = "C17", collectorNumber = "188")
public class PhantomNishoba extends Card {

    public PhantomNishoba() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(7)));
        addStaticEffect(new PreventDamageAndRemovePlusOnePlusOneCountersEffect(true));
        addTrigger(Triggers.dealsDamage(), new GainLifeEffect(new EventValue()));
    }
}
