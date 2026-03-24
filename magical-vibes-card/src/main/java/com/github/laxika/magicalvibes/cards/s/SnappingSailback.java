package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfEffect;

@CardRegistration(set = "XLN", collectorNumber = "208")
public class SnappingSailback extends Card {

    public SnappingSailback() {
        // Enrage — Whenever this creature is dealt damage, put a +1/+1 counter on it.
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new PutCounterOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
