package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "39")
@CardRegistration(set = "SOI", collectorNumber = "47")
@CardRegistration(set = "MID", collectorNumber = "40")
@CardRegistration(set = "DBL", collectorNumber = "40")
public class UnrulyMob extends Card {

    public UnrulyMob() {
        // Whenever another creature you control dies, put a +1/+1 counter on Unruly Mob.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
