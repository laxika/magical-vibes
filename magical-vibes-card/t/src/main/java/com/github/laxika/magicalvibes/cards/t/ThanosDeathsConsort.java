package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "508")
public class ThanosDeathsConsort extends Card {

    public ThanosDeathsConsort() {
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
