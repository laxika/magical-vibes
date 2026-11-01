package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "23")
@CardRegistration(set = "FDN", collectorNumber = "301")
@CardRegistration(set = "FDN", collectorNumber = "370")
@CardRegistration(set = "FDN", collectorNumber = "448")
public class SkyknightSquire extends Card {

    public SkyknightSquire() {
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE));

        SourceCounterThreshold threePlusOnePlusOneCounters =
                new SourceCounterThreshold(3, CounterType.PLUS_ONE_PLUS_ONE);
        addStaticEffect(new ConditionalEffect(
                threePlusOnePlusOneCounters,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
        addStaticEffect(new ConditionalEffect(
                threePlusOnePlusOneCounters,
                new GrantSubtypeEffect(CardSubtype.KNIGHT, GrantScope.SELF)));
    }
}
