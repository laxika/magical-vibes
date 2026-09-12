package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealPlanarCardsUntilTwoPlanesAndPlaneswalkEffect;

@CardRegistration(set = "OPC2", collectorNumber = "7")
public class SpatialMerging extends Card {

    public SpatialMerging() {
        addEffect(EffectSlot.ENCOUNTER_TRIGGERED, new RevealPlanarCardsUntilTwoPlanesAndPlaneswalkEffect());
    }
}
