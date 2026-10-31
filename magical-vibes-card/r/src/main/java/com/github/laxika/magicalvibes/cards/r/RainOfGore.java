package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RainOfGoreEffect;

@CardRegistration(set = "DIS", collectorNumber = "126")
public class RainOfGore extends Card {

    public RainOfGore() {
        addStaticEffect(new RainOfGoreEffect());
    }
}
