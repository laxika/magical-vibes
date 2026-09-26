package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "MSC", collectorNumber = "530")
public class BlueMarvelAdamBrashear extends Card {

    public BlueMarvelAdamBrashear() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
