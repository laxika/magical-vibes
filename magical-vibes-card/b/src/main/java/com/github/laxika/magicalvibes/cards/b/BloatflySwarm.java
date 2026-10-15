package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndRemovePlusOnePlusOneCountersEffect;

@CardRegistration(set = "PIP", collectorNumber = "42")
@CardRegistration(set = "PIP", collectorNumber = "570")
public class BloatflySwarm extends Card {

    public BloatflySwarm() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(5)));
        addEffect(EffectSlot.STATIC,
                PreventDamageAndRemovePlusOnePlusOneCountersEffect.withEachPlayerRadCounterFollowUp());
    }
}
