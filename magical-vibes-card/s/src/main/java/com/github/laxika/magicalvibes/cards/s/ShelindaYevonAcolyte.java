package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureSourcePowerBranchEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIC", collectorNumber = "94")
@CardRegistration(set = "FIC", collectorNumber = "184")
public class ShelindaYevonAcolyte extends Card {

    public ShelindaYevonAcolyte() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new EnteringCreatureSourcePowerBranchEffect(
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                        PutCountersEffect.onTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE, 1)));
    }
}
