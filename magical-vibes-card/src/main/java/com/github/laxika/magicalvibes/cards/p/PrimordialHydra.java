package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;

@CardRegistration(set = "M12", collectorNumber = "189")
@CardRegistration(set = "M13", collectorNumber = "183")
public class PrimordialHydra extends Card {

    public PrimordialHydra() {
        // This creature enters with X +1/+1 counters on it.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));

        // At the beginning of your upkeep, double the number of +1/+1 counters on this creature:
        // add as many as it already has.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE,
                        new CountersOnSource(CounterType.PLUS_ONE_PLUS_ONE)));

        // This creature has trample as long as it has ten or more +1/+1 counters on it.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(10, CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.SELF)));
    }
}
