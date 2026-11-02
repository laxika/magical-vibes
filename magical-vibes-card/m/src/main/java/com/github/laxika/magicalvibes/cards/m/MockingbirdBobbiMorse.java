package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "522")
@CardRegistration(set = "MSC", collectorNumber = "842")
public class MockingbirdBobbiMorse extends Card {

    public MockingbirdBobbiMorse() {
        // Whenever this creature is dealt damage, put a +1/+1 counter on it.
        addTrigger(Triggers.dealtDamage(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
