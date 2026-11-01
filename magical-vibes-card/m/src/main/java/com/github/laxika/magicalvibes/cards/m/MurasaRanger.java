package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "178")
public class MurasaRanger extends Card {

    public MurasaRanger() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new MayPayManaEffect(
                "{3}{G}",
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                "Pay {3}{G} to put two +1/+1 counters on Murasa Ranger?"));
    }
}
