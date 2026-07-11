package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;

@CardRegistration(set = "SOM", collectorNumber = "185")
public class Necropede extends Card {

    public Necropede() {
        // When Necropede dies, you may put a -1/-1 counter on target creature.
        addEffect(EffectSlot.ON_DEATH,
                new MayEffect(new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE),
                        "Put a -1/-1 counter on target creature?"));
    }
}
