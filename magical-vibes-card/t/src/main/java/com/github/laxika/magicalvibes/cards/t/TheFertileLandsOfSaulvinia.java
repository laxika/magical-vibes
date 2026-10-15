package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AddOneOfEachManaTypeProducedByLandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealPlanarCardsUntilPlaneAndTriggerChaosEffect;

@CardRegistration(set = "MOC", collectorNumber = "50")
public class TheFertileLandsOfSaulvinia extends Card {

    public TheFertileLandsOfSaulvinia() {
        addEffect(EffectSlot.ON_ANY_PLAYER_TAPS_LAND,
                new AddOneOfEachManaTypeProducedByLandEffect(false));
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new RevealPlanarCardsUntilPlaneAndTriggerChaosEffect());
    }
}
