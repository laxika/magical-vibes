package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndRemovePlusOnePlusOneCountersEffect;

@CardRegistration(set = "JUD", collectorNumber = "140")
public class PhantomNishoba extends Card {

    public PhantomNishoba() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(7)));
        addEffect(EffectSlot.STATIC, new PreventDamageAndRemovePlusOnePlusOneCountersEffect());
        addEffect(EffectSlot.ON_SELF_DEALS_DAMAGE, new GainLifeEffect(new EventValue()));
    }
}
