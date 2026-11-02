package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOS", collectorNumber = "143")
public class ComfortingCounsel extends Card {

    public ComfortingCounsel() {
        addTrigger(Triggers.youGainLife(), new PutCountersOnSelfEffect(CounterType.GROWTH));
        addStaticEffect(new ConditionalEffect(new SourceCounterThreshold(5, CounterType.GROWTH), new StaticBoostEffect(3, 3, GrantScope.OWN_CREATURES)
        ));
    }
}
