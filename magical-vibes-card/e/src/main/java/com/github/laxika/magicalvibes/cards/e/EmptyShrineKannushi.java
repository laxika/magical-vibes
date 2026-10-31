package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsOfPermanentsYouControlEffect;

@CardRegistration(set = "BOK", collectorNumber = "2")
public class EmptyShrineKannushi extends Card {

    public EmptyShrineKannushi() {
        addStaticEffect(new ProtectionFromColorsOfPermanentsYouControlEffect());
    }
}
