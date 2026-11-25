package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "83")
@CardRegistration(set = "TMT", collectorNumber = "217")
@CardRegistration(set = "TMT", collectorNumber = "285")
@CardRegistration(set = "TMT", collectorNumber = "295")
public class SuperShredder extends Card {

    public SuperShredder() {
        // Whenever another permanent leaves the battlefield, put a +1/+1 counter on Super Shredder.
        addTrigger(Triggers.leavesBattlefield(null, ObjectRelation.ANOTHER),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
