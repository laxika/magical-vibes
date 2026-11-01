package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.ChooseCounterTypeOnEnterEffect;

@CardRegistration(set = "IKO", collectorNumber = "115")
public class FerociousTigorilla extends Card {

    public FerociousTigorilla() {
        addStaticEffect(new ChooseCounterTypeOnEnterEffect(CounterType.TRAMPLE, CounterType.MENACE));
    }
}
