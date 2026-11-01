package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "199")
public class ThreeTreeScribe extends Card {

    public ThreeTreeScribe() {
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.leavesBattlefieldWithoutDying(Triggers.CREATURE, ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1));
    }
}
