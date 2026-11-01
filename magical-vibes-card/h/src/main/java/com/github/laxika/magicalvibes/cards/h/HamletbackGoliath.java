package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEqualToEnteringPowerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "173")
@CardRegistration(set = "M13", collectorNumber = "136")
@CardRegistration(set = "C15", collectorNumber = "156")
@CardRegistration(set = "CM2", collectorNumber = "102")
public class HamletbackGoliath extends Card {

    public HamletbackGoliath() {
        // Whenever another creature enters, you may put X +1/+1 counters on this creature, where X is that creature's power.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new PutCountersOnSourceEqualToEnteringPowerEffect(1, 1, true));
    }
}
