package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceIsCreature;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;

@CardRegistration(set = "RIX", collectorNumber = "142")
@CardRegistration(set = "FIC", collectorNumber = "312")
@CardRegistration(set = "MSC", collectorNumber = "179")
@CardRegistration(set = "MSC", collectorNumber = "386")
@CardRegistration(set = "BLC", collectorNumber = "231")
public class PathOfDiscovery extends Card {

    public PathOfDiscovery() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new ExploreEffect(PermanentReference.TRIGGERING));
        // "Whenever a creature you control enters" includes Path itself when it enters as an animated creature.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withEventCondition(new SourceIsCreature()),
                new ExploreEffect());
    }
}
