package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealPlanarCardsUntilPlaneAndTriggerChaosEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOC", collectorNumber = "50")
public class TheFertileLandsOfSaulvinia extends Card {

    public TheFertileLandsOfSaulvinia() {
        addTrigger(Triggers.playerTapsLandForMana(),
                new AddOneOfEachManaTypeProducedByLandEffect());
        addTrigger(Triggers.chaosEnsues(),
                new RevealPlanarCardsUntilPlaneAndTriggerChaosEffect());
    }
}
