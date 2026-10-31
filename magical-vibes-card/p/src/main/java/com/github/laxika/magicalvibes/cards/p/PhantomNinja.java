package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;

@CardRegistration(set = "MH1", collectorNumber = "62")
public class PhantomNinja extends Card {

    public PhantomNinja() {
        addStaticEffect(new CantBeBlockedEffect());
    }
}
