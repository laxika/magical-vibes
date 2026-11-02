package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealPlanarCardsUntilFivePlanesEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "2")
@CardRegistration(set = "OPCA", collectorNumber = "2")
public class InterplanarTunnel extends Card {

    public InterplanarTunnel() {
        addTrigger(Triggers.encounterThis(), new RevealPlanarCardsUntilFivePlanesEffect());
    }
}
