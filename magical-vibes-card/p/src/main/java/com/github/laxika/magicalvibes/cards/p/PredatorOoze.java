package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "124")
@CardRegistration(set = "C20", collectorNumber = "185")
public class PredatorOoze extends Card {

    public PredatorOoze() {
        addTrigger(Triggers.attacks(), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
        addTrigger(Triggers.creatureDies(ObjectRelation.DAMAGED_BY_THIS), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
