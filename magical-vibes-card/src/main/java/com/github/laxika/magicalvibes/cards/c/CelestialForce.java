package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "CMD", collectorNumber = "10")
public class CelestialForce extends Card {

    public CelestialForce() {
        // At the beginning of each upkeep, you gain 3 life.
        addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new GainLifeEffect(3));
    }
}
