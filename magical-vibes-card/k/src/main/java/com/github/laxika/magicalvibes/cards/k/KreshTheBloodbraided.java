package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentPower;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALA", collectorNumber = "178")
@CardRegistration(set = "V11", collectorNumber = "5")
@CardRegistration(set = "NCC", collectorNumber = "345")
public class KreshTheBloodbraided extends Card {

    public KreshTheBloodbraided() {
        // Whenever another creature dies, you may put X +1/+1 counters on Kresh, where X is that creature's power.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER),
                new MayEffect(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE,
                        new TriggeringPermanentPower()),
                        "Put +1/+1 counters equal to that creature's power on Kresh the Bloodbraided?"));
    }
}
