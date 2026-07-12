package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "SHM", collectorNumber = "65")
public class DuskUrchins extends Card {

    public DuskUrchins() {
        // Whenever this creature attacks or blocks, put a -1/-1 counter on it.
        addEffect(EffectSlot.ON_ATTACK, new PutCountersOnSourceEffect(-1, -1, 1));
        addEffect(EffectSlot.ON_BLOCK, new PutCountersOnSourceEffect(-1, -1, 1));

        // When this creature dies, draw a card for each -1/-1 counter on it.
        addEffect(EffectSlot.ON_DEATH,
                new DrawCardForEachDyingSourceCounterEffect(CounterType.MINUS_ONE_MINUS_ONE));
    }
}
