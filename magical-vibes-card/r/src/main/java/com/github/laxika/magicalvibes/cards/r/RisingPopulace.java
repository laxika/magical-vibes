package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WAR", collectorNumber = "29")
@CardRegistration(set = "RVR", collectorNumber = "25")
public class RisingPopulace extends Card {

    public RisingPopulace() {
        // Whenever another creature or planeswalker you control dies, put a +1/+1 counter on this creature.
        addTrigger(Triggers.dies(Triggers.CREATURE_OR_PLANESWALKER, ObjectRelation.ANOTHER_YOU_CONTROL),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
