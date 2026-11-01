package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenDealDamageToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DIS", collectorNumber = "61")
public class FlameKinWarScout extends Card {

    public FlameKinWarScout() {
        // When another creature enters, sacrifice this creature. If you do, it deals 4 damage to that creature.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                new SacrificeSelfThenDealDamageToEnteringCreatureEffect(4));
    }
}
