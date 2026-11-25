package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DIS", collectorNumber = "61")
public class FlameKinWarScout extends Card {

    public FlameKinWarScout() {
        // When another creature enters, sacrifice this creature. If you do, it deals 4 damage to that creature.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                SequenceEffect.of(
                        new SacrificeSelfEffect(),
                        new ConditionalEffect(HappenedThisWay.sacrificed(),
                                DealDamageEffect.to(Subjects.triggering(), 4))));
    }
}
