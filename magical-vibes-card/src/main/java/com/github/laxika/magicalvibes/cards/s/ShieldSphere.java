package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "ALL", collectorNumber = "129")
public class ShieldSphere extends Card {

    public ShieldSphere() {
        // Whenever this creature blocks, put a -0/-1 counter on it. The counter lands on trigger
        // resolution (before combat damage), unlike the end-of-combat schedulers.
        addEffect(EffectSlot.ON_BLOCK, new PutCountersOnSelfEffect(CounterType.MINUS_ZERO_MINUS_ONE));
    }
}
