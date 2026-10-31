package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ThreatsUndetectedEffect;

@CardRegistration(set = "DMU", collectorNumber = "185")
public class ThreatsUndetected extends Card {

    public ThreatsUndetected() {
        addSpellEffect(new ThreatsUndetectedEffect());
    }
}
