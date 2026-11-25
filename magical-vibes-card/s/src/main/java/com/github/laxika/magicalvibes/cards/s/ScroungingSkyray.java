package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DFT", collectorNumber = "60")
public class ScroungingSkyray extends Card {

    public ScroungingSkyray() {
        addTrigger(Triggers.youDiscardOneOrMoreCards(),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()));

        addCycling("{2}");
    }
}
