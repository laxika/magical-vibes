package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.effect.RemoveCountersEffect;

@CardRegistration(set = "MMQ", collectorNumber = "176")
public class BloodHound extends Card {

    public BloodHound() {
        addTrigger(Triggers.youAreDealtDamage(),
                new MayEffect(
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, new EventValue()),
                        "Put that many +1/+1 counters on Blood Hound?"));
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                RemoveCountersEffect.allFromSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
