package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAllCountersEffect;

@CardRegistration(set = "MMQ", collectorNumber = "176")
public class BloodHound extends Card {

    public BloodHound() {
        addEffect(EffectSlot.ON_CONTROLLER_DEALT_DAMAGE,
                new MayEffect(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()),
                        "Put that many +1/+1 counters on Blood Hound?"));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new RemoveAllCountersEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
