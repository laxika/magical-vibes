package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EchoEffect;

@CardRegistration(set = "USG", collectorNumber = "17")
public class HeraldOfSerra extends Card {

    public HeraldOfSerra() {
        addStaticEffect(new EchoEffect("{2}{W}{W}"));
    }
}
