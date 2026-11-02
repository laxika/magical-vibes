package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentPowerGreaterThanSourcePower;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "141")
public class PeltCollector extends Card {

    public PeltCollector() {
        CardEffect growWhenLarger = new TriggeringPermanentConditionalEffect(
                new PermanentNotPredicate(new PermanentPowerAtMostSourcePowerPredicate()),
                new ConditionalEffect(
                        new TriggeringPermanentPowerGreaterThanSourcePower(),
                        new PutCountersOnSourceEffect(1, 1, 1)));
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL), growWhenLarger);
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new TriggeringPermanentPowerGreaterThanSourcePower()),
                new PutCountersOnSourceEffect(1, 1, 1));

        addStaticEffect(new ConditionalEffect(
                        new SourceCounterThreshold(3, CounterType.PLUS_ONE_PLUS_ONE),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)));
    }
}
