package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "37")
public class VengefulTownsfolk extends Card {

    public VengefulTownsfolk() {
        // Whenever one or more other creatures you control die, put a +1/+1 counter on this creature.
        addTrigger(new TriggerCondition(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL)),
                TriggerOptions.ONE_OR_MORE, PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
