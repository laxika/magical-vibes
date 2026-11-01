package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "200")
@CardRegistration(set = "SLZ", collectorNumber = "103")
@CardRegistration(set = "SLZ", collectorNumber = "224")
@CardRegistration(set = "SLZ", collectorNumber = "345")
public class EternityVessel extends Card {

    public EternityVessel() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.CHARGE, new ControllerLifeTotal()));
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                new MayEffect(
                        new SetLifeTotalEffect(new CountersOnSource(CounterType.CHARGE)),
                        "Have your life total become the number of charge counters on Eternity Vessel?"));
    }
}
