package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ChooseCounterTypeOnEnterEffect;

@CardRegistration(set = "IKO", collectorNumber = "15")
public class HelicaGlider extends Card {

    public HelicaGlider() {
        addStaticEffect(new ChooseCounterTypeOnEnterEffect(CounterType.FLYING, CounterType.FIRST_STRIKE));
    }
}
