package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EchoEffect;

@CardRegistration(set = "USG", collectorNumber = "230")
public class Acridian extends Card {

    public Acridian() {
        addStaticEffect(new EchoEffect("{1}{G}"));
    }
}
