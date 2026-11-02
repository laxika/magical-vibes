package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DST", collectorNumber = "73")
@CardRegistration(set = "DDH", collectorNumber = "18")
@CardRegistration(set = "C21", collectorNumber = "184")
public class AgelessEntity extends Card {

    public AgelessEntity() {
        // Whenever you gain life, put that many +1/+1 counters on this creature.
        addTrigger(Triggers.youGainLife(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()));
    }
}
