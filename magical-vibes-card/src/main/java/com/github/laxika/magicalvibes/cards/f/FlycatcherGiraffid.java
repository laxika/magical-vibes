package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCounterTypeOnEnterEffect;

@CardRegistration(set = "IKO", collectorNumber = "153")
public class FlycatcherGiraffid extends Card {

    public FlycatcherGiraffid() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseCounterTypeOnEnterEffect(CounterType.REACH, CounterType.VIGILANCE));
    }
}
