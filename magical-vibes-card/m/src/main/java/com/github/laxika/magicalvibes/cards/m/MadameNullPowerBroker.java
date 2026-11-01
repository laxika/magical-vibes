package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeAndPutCountersOnEnteringCreatureEqualToPowerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "66")
@CardRegistration(set = "TMT", collectorNumber = "265")
public class MadameNullPowerBroker extends Card {

    public MadameNullPowerBroker() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayPayLifeAndPutCountersOnEnteringCreatureEqualToPowerEffect());
    }
}
