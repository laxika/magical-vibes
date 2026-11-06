package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5DN", collectorNumber = "103")
@CardRegistration(set = "MMA", collectorNumber = "200")
public class ArcboundWanderer extends Card {

    public ArcboundWanderer() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue(), true));

        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new MayEffect(PutCounterOnTargetForEachDyingSourceCounterEffect.modularAbility(),
                        PutCounterOnTargetForEachDyingSourceCounterEffect.MODULAR_PROMPT));
    }
}
