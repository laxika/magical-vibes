package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "15")
@CardRegistration(set = "TMT", collectorNumber = "211")
@CardRegistration(set = "TMT", collectorNumber = "281")
@CardRegistration(set = "TMT", collectorNumber = "291")
public class LeonardoCuttingEdge extends Card {

    public LeonardoCuttingEdge() {
        addSneak("{W}");
        addTrigger(Triggers.youGainLife(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
