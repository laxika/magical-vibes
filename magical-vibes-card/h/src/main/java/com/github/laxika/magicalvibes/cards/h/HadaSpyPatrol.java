package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "71")
@CardRegistration(set = "C13", collectorNumber = "46")
@CardRegistration(set = "CMA", collectorNumber = "40")
public class HadaSpyPatrol extends Card {

    public HadaSpyPatrol() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(PutCountersEffect.onSelf(CounterType.LEVEL)),
                "Level up {2}{U} ({2}{U}: Put a level counter on this. Level up only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addStaticEffect(new RemoveKeywordEffect(Keyword.SHROUD, Subjects.source()));
        addStaticEffect(new ConditionalEffect(
                new AllOf(List.of(
                        new SourceCounterThreshold(1, CounterType.LEVEL),
                        new NotCondition(new SourceCounterThreshold(3, CounterType.LEVEL)))),
                new SetBasePowerToughnessEffect(2, 2, Subjects.source())));
        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(3, CounterType.LEVEL),
                new SetBasePowerToughnessEffect(3, 3, Subjects.source())));
        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(1, CounterType.LEVEL),
                new GrantEffectEffect(new CantBeBlockedEffect(), Subjects.source())));
        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(3, CounterType.LEVEL),
                new GrantKeywordEffect(Keyword.SHROUD, Subjects.source())));
    }
}
