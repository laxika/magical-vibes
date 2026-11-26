package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;

@CardRegistration(set = "UDS", collectorNumber = "111")
public class Magnify extends Card {

    public Magnify() {
        addSpellEffect(BoostEffect.allCreatures(1, 1));
    }
}
