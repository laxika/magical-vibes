package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnTriggeringPermanent;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongControlledCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M14", collectorNumber = "198")
@CardRegistration(set = "C20", collectorNumber = "194")
public class VastwoodHydra extends Card {

    public VastwoodHydra() {
        // This creature enters with X +1/+1 counters on it.
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));

        // When this creature dies, you may distribute a number of +1/+1 counters equal to the number
        // of +1/+1 counters on this creature among any number of creatures you control.
        // Does not target: the division is chosen as the ability resolves.
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new MayEffect(new DistributeCountersAmongControlledCreaturesEffect(CounterType.PLUS_ONE_PLUS_ONE,
                        new CountersOnTriggeringPermanent(CounterType.PLUS_ONE_PLUS_ONE)),
                        "Distribute a +1/+1 counter among creatures you control for each +1/+1 counter on this creature?"));
    }
}
