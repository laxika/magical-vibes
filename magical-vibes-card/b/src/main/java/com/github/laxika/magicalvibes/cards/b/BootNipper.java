package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ChooseCounterTypeOnEnterEffect;

@CardRegistration(set = "IKO", collectorNumber = "76")
public class BootNipper extends Card {

    public BootNipper() {
        addStaticEffect(new ChooseCounterTypeOnEnterEffect(CounterType.DEATHTOUCH, CounterType.LIFELINK));
    }
}
