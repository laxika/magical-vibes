package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALL", collectorNumber = "129")
@CardRegistration(set = "ME1", collectorNumber = "166")
public class ShieldSphere extends Card {

    public ShieldSphere() {
        // Whenever this creature blocks, put a -0/-1 counter on it. The counter lands on trigger
        // resolution (before combat damage), unlike the end-of-combat schedulers.
        addTrigger(Triggers.blocks(),
                TriggerOptions.ONE_OR_MORE,
                new PutCountersOnSelfEffect(CounterType.MINUS_ZERO_MINUS_ONE));
    }
}
