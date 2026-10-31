package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReduceControllerMaxHandSizeEffect;

@CardRegistration(set = "ODY", collectorNumber = "107")
public class ThoughtNibbler extends Card {

    public ThoughtNibbler() {
        addStaticEffect(new ReduceControllerMaxHandSizeEffect(2));
    }
}
