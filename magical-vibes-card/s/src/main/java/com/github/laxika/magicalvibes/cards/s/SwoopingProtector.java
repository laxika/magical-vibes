package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;

@CardRegistration(set = "SNC", collectorNumber = "33")
public class SwoopingProtector extends Card {

    public SwoopingProtector() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.SHIELD, new Fixed(1)));
    }
}
