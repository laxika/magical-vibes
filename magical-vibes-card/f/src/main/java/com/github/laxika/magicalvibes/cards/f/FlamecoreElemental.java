package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EchoEffect;

@CardRegistration(set = "TSP", collectorNumber = "154")
public class FlamecoreElemental extends Card {

    public FlamecoreElemental() {
        addStaticEffect(new EchoEffect("{2}{R}{R}"));
    }
}
