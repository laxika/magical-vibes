package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ChooseCounterTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEqualToSourceCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "71")
@CardRegistration(set = "NCC", collectorNumber = "171")
public class DenryKlinEditorInChief extends Card {

    public DenryKlinEditorInChief() {
        addStaticEffect(new ChooseCounterTypeOnEnterEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, CounterType.FIRST_STRIKE, CounterType.VIGILANCE));
        addTrigger(Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new SourceCounterThreshold(1, CounterType.ANY)),
                new PutCountersOnEnteringCreatureEqualToSourceCountersEffect());
    }
}
