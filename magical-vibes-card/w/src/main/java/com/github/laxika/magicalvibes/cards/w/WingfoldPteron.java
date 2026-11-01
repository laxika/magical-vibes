package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ChooseCounterTypeOnEnterEffect;

@CardRegistration(set = "IKO", collectorNumber = "71")
public class WingfoldPteron extends Card {

    public WingfoldPteron() {
        addStaticEffect(new ChooseCounterTypeOnEnterEffect(CounterType.FLYING, CounterType.HEXPROOF));
    }
}
