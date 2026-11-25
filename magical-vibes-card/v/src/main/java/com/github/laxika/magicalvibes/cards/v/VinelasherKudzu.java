package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "189")
@CardRegistration(set = "DDM", collectorNumber = "50")
@CardRegistration(set = "GK2", collectorNumber = "114")
public class VinelasherKudzu extends Card {

    public VinelasherKudzu() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
