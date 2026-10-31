package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "15")
@CardRegistration(set = "DDG", collectorNumber = "2")
@CardRegistration(set = "DDP", collectorNumber = "3")
public class CaravanEscort extends Card {

    public CaravanEscort() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new PutCountersOnSelfEffect(CounterType.LEVEL)),
                "Level up {2} ({2}: Put a level counter on this. Level up only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addStaticEffect(new RemoveKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF));
        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(1, CounterType.LEVEL),
                new SetBasePowerToughnessEffect(2, 2, GrantScope.SELF)));
        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(5, CounterType.LEVEL),
                new SetBasePowerToughnessEffect(5, 5, GrantScope.SELF)));
        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(5, CounterType.LEVEL),
                new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.SELF)));
    }
}
