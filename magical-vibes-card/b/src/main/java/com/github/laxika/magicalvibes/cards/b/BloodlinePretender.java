package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "235")
@CardRegistration(set = "MOC", collectorNumber = "350")
public class BloodlinePretender extends Card {

    public BloodlinePretender() {
        // As this creature enters, choose a creature type.
        addStaticEffect(new ChooseSubtypeOnEnterEffect());

        // Whenever another creature you control of the chosen type enters, put a +1/+1 counter
        // on this creature.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasSourceChosenSubtypePredicate(),
                        new PutCountersOnSourceEffect(1, 1, 1)));
    }
}
