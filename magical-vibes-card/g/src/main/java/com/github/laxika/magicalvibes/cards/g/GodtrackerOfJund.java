package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnteringCreatureMinPowerConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "55")
public class GodtrackerOfJund extends Card {

    public GodtrackerOfJund() {
        // Whenever a creature you control with power 5 or greater enters, you may put a +1/+1 counter on this creature.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.YOU_CONTROL),
                new EnteringCreatureMinPowerConditionalEffect(5,
                        new MayEffect(new PutCountersOnSourceEffect(1, 1, 1),
                                "Put a +1/+1 counter on this creature?")));
    }
}
