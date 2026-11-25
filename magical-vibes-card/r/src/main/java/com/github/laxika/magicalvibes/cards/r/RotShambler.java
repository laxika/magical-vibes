package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "187")
@CardRegistration(set = "CMM", collectorNumber = "318")
public class RotShambler extends Card {

    public RotShambler() {
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
