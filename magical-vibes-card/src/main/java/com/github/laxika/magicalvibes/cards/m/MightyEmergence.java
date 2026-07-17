package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureMinPowerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;

@CardRegistration(set = "ALA", collectorNumber = "137")
public class MightyEmergence extends Card {

    public MightyEmergence() {
        // Whenever a creature you control with power 5 or greater enters, you may put two +1/+1 counters on it.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMinPowerConditionalEffect(5,
                        new PutCountersOnEnteringCreatureEffect(2)));
    }
}
