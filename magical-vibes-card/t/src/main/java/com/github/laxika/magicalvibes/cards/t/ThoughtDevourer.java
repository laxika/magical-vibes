package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReduceControllerMaxHandSizeEffect;

@CardRegistration(set = "ODY", collectorNumber = "105")
public class ThoughtDevourer extends Card {

    public ThoughtDevourer() {
        addStaticEffect(new ReduceControllerMaxHandSizeEffect(4));
    }
}
