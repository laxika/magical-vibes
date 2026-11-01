package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HBG", collectorNumber = "89")
public class FlamingFistOfficer extends Card {

    public FlamingFistOfficer() {
        // Whenever another creature you control leaves the battlefield, put a +1/+1 counter on this creature.
        addTrigger(Triggers.leavesBattlefield(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
