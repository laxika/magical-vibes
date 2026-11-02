package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnConvokeCreaturesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "30")
@CardRegistration(set = "MOC", collectorNumber = "215")
public class VeneratedLoxodon extends Card {

    public VeneratedLoxodon() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PutCounterOnConvokeCreaturesEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
