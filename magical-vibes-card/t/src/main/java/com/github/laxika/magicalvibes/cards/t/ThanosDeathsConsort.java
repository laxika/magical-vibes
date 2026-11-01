package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "508")
public class ThanosDeathsConsort extends Card {

    public ThanosDeathsConsort() {
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
