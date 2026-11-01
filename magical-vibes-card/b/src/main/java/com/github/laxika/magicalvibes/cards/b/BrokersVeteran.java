package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "36")
public class BrokersVeteran extends Card {

    public BrokersVeteran() {
        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.dies(ObjectRelation.THIS),
                new PutCounterOnTargetPermanentEffect(CounterType.SHIELD, 1));
    }
}
