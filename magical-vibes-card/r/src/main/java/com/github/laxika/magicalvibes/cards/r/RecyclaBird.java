package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MB1", collectorNumber = "28")
public class RecyclaBird extends Card {

    public RecyclaBird() {
        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.dies(ObjectRelation.THIS),
                new PutCounterOnTargetPermanentEffect(CounterType.FLYING));
    }
}
