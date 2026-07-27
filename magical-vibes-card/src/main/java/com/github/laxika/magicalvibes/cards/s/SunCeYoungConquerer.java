package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PTK", collectorNumber = "55")
public class SunCeYoungConquerer extends Card {

    public SunCeYoungConquerer() {
        // Horsemanship is auto-loaded from Scryfall keywords.
        // When Sun Ce enters, you may return target creature to its owner's hand.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new MayEffect(ReturnToHandEffect.target(),
                                "Return target creature to its owner's hand?"));
    }
}
