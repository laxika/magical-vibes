package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureExactStatsConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "77")
@CardRegistration(set = "CMD", collectorNumber = "225")
public class SigilCaptain extends Card {

    public SigilCaptain() {
        // Whenever a creature you control enters, if that creature is 1/1, put two +1/+1 counters on it.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                new EnteringCreatureExactStatsConditionalEffect(1, 1,
                        new PutCountersOnEnteringCreatureEffect(2, false)));
    }
}
