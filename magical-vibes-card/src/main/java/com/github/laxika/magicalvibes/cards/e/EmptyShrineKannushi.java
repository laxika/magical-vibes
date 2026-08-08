package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsOfPermanentsYouControlEffect;

@CardRegistration(set = "BOK", collectorNumber = "2")
public class EmptyShrineKannushi extends Card {

    public EmptyShrineKannushi() {
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsOfPermanentsYouControlEffect());
    }
}
