package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MoveCountersFromControlledPermanentsToSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RNA", collectorNumber = "175")
public class GallopingLizrog extends Card {

    public GallopingLizrog() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MoveCountersFromControlledPermanentsToSourceEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, new PermanentIsCreaturePredicate(), 2, true));
    }
}
