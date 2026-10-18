package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "DFT", collectorNumber = "60")
public class ScroungingSkyray extends Card {

    public ScroungingSkyray() {
        addEffect(EffectSlot.ON_CONTROLLER_DISCARD_EVENT,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()));

        addCycling("{2}");
    }
}
