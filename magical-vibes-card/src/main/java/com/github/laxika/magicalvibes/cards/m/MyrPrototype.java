package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessPaysPerCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "MRD", collectorNumber = "214")
public class MyrPrototype extends Card {

    public MyrPrototype() {
        // At the beginning of your upkeep, put a +1/+1 counter on this creature.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new PutCountersOnSourceEffect(1, 1, 1));

        // This creature can't attack or block unless you pay {1} for each +1/+1 counter on it.
        addEffect(EffectSlot.STATIC,
                new CantAttackOrBlockUnlessPaysPerCounterEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
