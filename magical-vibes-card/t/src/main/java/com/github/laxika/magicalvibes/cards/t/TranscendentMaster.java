package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import java.util.List;

@CardRegistration(set = "ROE", collectorNumber = "51")
@CardRegistration(set = "MB2", collectorNumber = "23")
public class TranscendentMaster extends Card {

    public TranscendentMaster() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(PutCountersEffect.onSelf(CounterType.LEVEL)),
                "Level up {1} ({1}: Put a level counter on this. Level up only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addStaticEffect(new RemoveKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.source()));
        addStaticEffect(new RemoveKeywordEffect(Keyword.LIFELINK, Subjects.source()));
        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(6, CounterType.LEVEL),
                new GrantKeywordEffect(Keyword.LIFELINK, Subjects.source())));
        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(6, CounterType.LEVEL),
                new SetBasePowerToughnessEffect(6, 6, Subjects.source())));
        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(12, CounterType.LEVEL),
                new SetBasePowerToughnessEffect(9, 9, Subjects.source())));
        addStaticEffect(new ConditionalEffect(
                new SourceCounterThreshold(12, CounterType.LEVEL),
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, Subjects.source())));
    }
}
