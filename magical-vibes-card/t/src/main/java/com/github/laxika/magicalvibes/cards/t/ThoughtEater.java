package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReduceControllerMaxHandSizeEffect;

@CardRegistration(set = "ODY", collectorNumber = "106")
public class ThoughtEater extends Card {

    public ThoughtEater() {
        addStaticEffect(new ReduceControllerMaxHandSizeEffect(3));
    }
}
