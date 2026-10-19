package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "MSH", collectorNumber = "149")
public class RedHulk extends Card {

    public RedHulk() {
        // Enrage — Whenever Red Hulk is dealt damage, put a +1/+1 counter on him. When you do, he deals
        // damage equal to the number of +1/+1 counters on him to any other target. The reflexive ability
        // chooses its target only when it goes on the stack, after the counter is placed.
        addEffect(EffectSlot.ON_DEALT_DAMAGE,
                SequenceEffect.of(
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        new QueueReflexiveAbilityEffect(DealDamageToAnyTargetEffect.toAnyOtherTarget(
                                new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE)))));
    }
}
