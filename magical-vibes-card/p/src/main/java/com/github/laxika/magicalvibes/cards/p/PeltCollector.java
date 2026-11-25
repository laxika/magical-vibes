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
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtMostSourcePowerPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "GRN", collectorNumber = "141")
public class PeltCollector extends Card {

    public PeltCollector() {
        CardEffect growWhenLarger = PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE);
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentNotPredicate(new PermanentPowerAtMostSourcePowerPredicate()))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new TriggeringPermanentPowerGreaterThanSourcePower()),
                growWhenLarger);
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER_YOU_CONTROL),
                TriggerOptions.DEFAULT.withInterveningIf(new TriggeringPermanentPowerGreaterThanSourcePower()),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));

        addStaticEffect(new ConditionalEffect(
                        new SourceCounterThreshold(3, CounterType.PLUS_ONE_PLUS_ONE),
                        new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.source())));
    }
}
