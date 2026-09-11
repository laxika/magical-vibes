package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealPlanarCardsUntilFivePlanesEffect;

@CardRegistration(set = "OPC2", collectorNumber = "2")
public class InterplanarTunnel extends Card {

    public InterplanarTunnel() {
        addEffect(EffectSlot.ENCOUNTER_TRIGGERED, new RevealPlanarCardsUntilFivePlanesEffect());
    }
}
