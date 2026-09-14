package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "EMN", collectorNumber = "89")
@CardRegistration(set = "GN2", collectorNumber = "29")
@CardRegistration(set = "SIR", collectorNumber = "111")
@CardRegistration(set = "GN3", collectorNumber = "51")
public class GavonyUnhallowed extends Card {

    public GavonyUnhallowed() {
        // Whenever another creature you control dies, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
