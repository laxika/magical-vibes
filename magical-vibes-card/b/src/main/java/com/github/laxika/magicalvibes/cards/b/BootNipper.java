package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCounterTypeOnEnterEffect;

@CardRegistration(set = "IKO", collectorNumber = "76")
public class BootNipper extends Card {

    public BootNipper() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseCounterTypeOnEnterEffect(CounterType.DEATHTOUCH, CounterType.LIFELINK));
    }
}
