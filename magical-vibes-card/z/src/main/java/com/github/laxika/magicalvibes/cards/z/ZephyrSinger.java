package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnConvokeCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "86")
@CardRegistration(set = "MOM", collectorNumber = "355")
public class ZephyrSinger extends Card {

    public ZephyrSinger() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PutCounterOnConvokeCreaturesEffect(CounterType.FLYING));
    }
}
