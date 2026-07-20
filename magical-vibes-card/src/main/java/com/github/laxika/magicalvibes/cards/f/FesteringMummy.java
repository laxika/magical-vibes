package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;

@CardRegistration(set = "AKH", collectorNumber = "91")
public class FesteringMummy extends Card {

    public FesteringMummy() {
        // When this creature dies, you may put a -1/-1 counter on target creature.
        addEffect(EffectSlot.ON_DEATH,
                new MayEffect(new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE),
                        "Put a -1/-1 counter on target creature?"));
    }
}
