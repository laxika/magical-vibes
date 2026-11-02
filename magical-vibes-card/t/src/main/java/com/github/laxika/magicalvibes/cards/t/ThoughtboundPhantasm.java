package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GRN", collectorNumber = "55")
@CardRegistration(set = "MKC", collectorNumber = "122")
public class ThoughtboundPhantasm extends Card {

    public ThoughtboundPhantasm() {
        addTrigger(Triggers.youSurveil(),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));
        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(3, CounterType.PLUS_ONE_PLUS_ONE),
                new CanAttackAsThoughNoDefenderEffect()));
    }
}
