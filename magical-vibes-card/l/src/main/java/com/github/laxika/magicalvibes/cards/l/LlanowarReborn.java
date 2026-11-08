package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromSourceToEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "171")
@CardRegistration(set = "DDL", collectorNumber = "72")
@CardRegistration(set = "C13", collectorNumber = "304")
@CardRegistration(set = "C15", collectorNumber = "293")
@CardRegistration(set = "LCC", collectorNumber = "341")
@CardRegistration(set = "C21", collectorNumber = "296")
@CardRegistration(set = "ARC", collectorNumber = "129")
public class LlanowarReborn extends Card {

    public LlanowarReborn() {
        addStaticEffect(new EntersTappedEffect());
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.GREEN));
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new Fixed(1)));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new SourceCounterThreshold(1, CounterType.PLUS_ONE_PLUS_ONE)),
                new MayEffect(new MoveCounterFromSourceToEnteringCreatureEffect(CounterType.PLUS_ONE_PLUS_ONE),
                        "Move a +1/+1 counter onto that creature?"));
    }
}
