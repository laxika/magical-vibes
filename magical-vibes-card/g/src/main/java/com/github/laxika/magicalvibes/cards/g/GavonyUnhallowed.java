package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EMN", collectorNumber = "89")
@CardRegistration(set = "GN2", collectorNumber = "29")
@CardRegistration(set = "SIR", collectorNumber = "111")
@CardRegistration(set = "GN3", collectorNumber = "51")
public class GavonyUnhallowed extends Card {

    public GavonyUnhallowed() {
        // Whenever another creature you control dies, put a +1/+1 counter on this creature.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
