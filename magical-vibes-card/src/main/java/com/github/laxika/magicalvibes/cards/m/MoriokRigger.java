package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "5DN", collectorNumber = "54")
public class MoriokRigger extends Card {

    public MoriokRigger() {
        addEffect(EffectSlot.ON_ANY_ARTIFACT_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                new MayEffect(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        "Put a +1/+1 counter on Moriok Rigger?"));
    }
}
