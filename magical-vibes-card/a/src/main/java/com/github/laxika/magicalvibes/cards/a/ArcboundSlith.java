package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetForEachDyingSourceCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DST", collectorNumber = "102")
@CardRegistration(set = "HOP", collectorNumber = "106")
public class ArcboundSlith extends Card {

    public ArcboundSlith() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)));

        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new MayEffect(PutCounterOnTargetForEachDyingSourceCounterEffect.modularAbility(),
                        PutCounterOnTargetForEachDyingSourceCounterEffect.MODULAR_PROMPT));

        addTrigger(Triggers.dealsCombatDamageToPlayer(), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
