package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OneOrMoreCreatureDeathTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "OTJ", collectorNumber = "37")
public class VengefulTownsfolk extends Card {

    public VengefulTownsfolk() {
        // Whenever one or more other creatures you control die, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new OneOrMoreCreatureDeathTriggerEffect(new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
