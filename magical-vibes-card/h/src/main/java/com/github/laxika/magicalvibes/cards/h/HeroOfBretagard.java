package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSubtypeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.PlayerRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "KHC", collectorNumber = "4")
public class HeroOfBretagard extends Card {

    public HeroOfBretagard() {
        PutCountersOnSelfEffect counters = new PutCountersOnSelfEffect(
                CounterType.PLUS_ONE_PLUS_ONE, new EventValue());
        addTrigger(new TriggerCondition(Triggers.exiledFrom(Set.of(Zone.HAND),
                null, ObjectRelation.YOU_OWN)), TriggerOptions.ONE_OR_MORE, counters);
        addTrigger(Triggers.exiledFrom(Set.of(Zone.BATTLEFIELD),
                null, ObjectRelation.ANOTHER).causedBy(PlayerRelation.YOU), counters);

        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(5, CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(5, CounterType.PLUS_ONE_PLUS_ONE),
                new GrantSubtypeEffect(CardSubtype.ANGEL, GrantScope.SELF)));
        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(10, CounterType.PLUS_ONE_PLUS_ONE),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)));
        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(10, CounterType.PLUS_ONE_PLUS_ONE),
                new GrantSubtypeEffect(CardSubtype.GOD, GrantScope.SELF)));
    }
}
