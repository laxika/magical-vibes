package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ARB", collectorNumber = "55")
public class GodtrackerOfJund extends Card {

    public GodtrackerOfJund() {
        // Whenever a creature you control with power 5 or greater enters, you may put a +1/+1 counter on this creature.
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(Triggers.CREATURE, new PermanentPowerAtLeastPredicate(5))),
                        ObjectRelation.YOU_CONTROL),
                new MayEffect(PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE), "Put a +1/+1 counter on this creature?"));
    }
}
