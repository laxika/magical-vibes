package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReduceCyclingCostEffect;

@CardRegistration(set = "USG", collectorNumber = "295")
@CardRegistration(set = "C20", collectorNumber = "241")
public class Fluctuator extends Card {

    public Fluctuator() {
        addStaticEffect(new ReduceCyclingCostEffect(2));
    }
}
