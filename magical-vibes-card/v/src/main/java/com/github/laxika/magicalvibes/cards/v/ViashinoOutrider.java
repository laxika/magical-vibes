package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EchoEffect;

@CardRegistration(set = "USG", collectorNumber = "223")
public class ViashinoOutrider extends Card {

    public ViashinoOutrider() {
        addStaticEffect(new EchoEffect("{2}{R}"));
    }
}
