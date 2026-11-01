package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5DN", collectorNumber = "54")
public class MoriokRigger extends Card {

    public MoriokRigger() {
        addTrigger(Triggers.dies(Triggers.ARTIFACT, ObjectRelation.ANY),
                new MayEffect(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        "Put a +1/+1 counter on Moriok Rigger?"));
    }
}
