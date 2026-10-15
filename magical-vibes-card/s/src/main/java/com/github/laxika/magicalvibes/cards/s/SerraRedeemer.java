package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureMaxPowerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;

@CardRegistration(set = "DMU", collectorNumber = "282")
public class SerraRedeemer extends Card {

    public SerraRedeemer() {
        // Whenever another creature you control with power 2 or less enters, put two +1/+1 counters on that creature.
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new EnteringCreatureMaxPowerConditionalEffect(2,
                        new PutCountersOnEnteringCreatureEffect(2, false)));
    }
}
