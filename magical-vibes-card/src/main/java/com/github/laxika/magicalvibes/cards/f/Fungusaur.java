package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "8ED", collectorNumber = "250")
public class Fungusaur extends Card {

    public Fungusaur() {
        // Whenever this creature is dealt damage, put a +1/+1 counter on it.
        addEffect(EffectSlot.ON_DEALT_DAMAGE,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
