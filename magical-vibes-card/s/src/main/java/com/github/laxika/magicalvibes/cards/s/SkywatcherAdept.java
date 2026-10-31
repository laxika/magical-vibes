package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "88")
public class SkywatcherAdept extends Card {

    public SkywatcherAdept() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(new PutCountersOnSelfEffect(CounterType.LEVEL)),
                "Level up {3} ({3}: Put a level counter on this. Level up only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addStaticEffect(new RemoveKeywordEffect(Keyword.FLYING, GrantScope.SELF));
        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(1, CounterType.LEVEL),
                new SetBasePowerToughnessEffect(2, 2, GrantScope.SELF)));
        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(1, CounterType.LEVEL),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)));
        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(3, CounterType.LEVEL),
                new SetBasePowerToughnessEffect(4, 2, GrantScope.SELF)));
    }
}
