package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentHasGreaterPowerOrToughnessThanSource;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "167")
public class EvolvingAdaptive extends Card {

    public EvolvingAdaptive() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.OIL, new Fixed(1)));
        addStaticEffect(new DynamicStaticBoostEffect(
                new CountersOnSource(CounterType.OIL),
                new CountersOnSource(CounterType.OIL),
                Subjects.source()));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new TriggeringPermanentHasGreaterPowerOrToughnessThanSource()),
                PutCountersEffect.onSelf(CounterType.OIL));
    }
}
