package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "137")
public class MightyEmergence extends Card {

    public MightyEmergence() {
        // Whenever a creature you control with power 5 or greater enters, you may put two +1/+1 counters on it.
        // The trigger includes Mighty Emergence itself entering.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, new PermanentPowerAtLeastPredicate(5))),
                        ObjectRelation.YOU_CONTROL),
                new PutCountersOnEnteringCreatureEffect(2));
    }
}
