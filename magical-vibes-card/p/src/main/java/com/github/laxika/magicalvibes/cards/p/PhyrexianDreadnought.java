package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfUnlessSacrificeCreaturesWithTotalPowerEffect;

@CardRegistration(set = "MIR", collectorNumber = "315")
public class PhyrexianDreadnought extends Card {

    public PhyrexianDreadnought() {
        // Trample is auto-loaded from Scryfall.
        // When Phyrexian Dreadnought enters, sacrifice it unless you sacrifice any number of
        // creatures with total power 12 or greater.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SacrificeSelfUnlessSacrificeCreaturesWithTotalPowerEffect(12));
    }
}
