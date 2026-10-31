package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;

@CardRegistration(set = "ONE", collectorNumber = "103")
public class PestilentSyphoner extends Card {

    public PestilentSyphoner() {
        addStaticEffect(new ToxicEffect(1));
    }
}
