package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.ManaSpentToCast;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "239")
public class VerazolTheSplitCurrent extends Card {

    public VerazolTheSplitCurrent() {
        addStaticEffect(new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new ManaSpentToCast()));
        addTrigger(Triggers.youCastSpell().kicked(),
                new MayEffect(new ConditionalEffect(new SourceCounterThreshold(2, CounterType.PLUS_ONE_PLUS_ONE),
                        SequenceEffect.of(
                                new RemoveCounterFromSourceEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                                new ConditionalEffect(HappenedThisWay.atLeast(StepKind.REMOVE_COUNTERS, 2),
                                        new CopyTriggeringSpellEffect(true)))),
                        "Remove two +1/+1 counters from Verazol to copy that spell?"));
    }
}
