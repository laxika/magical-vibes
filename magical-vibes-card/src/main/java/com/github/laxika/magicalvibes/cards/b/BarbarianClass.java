package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ClassLevelUpEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RollWithAdvantageEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "AFR", collectorNumber = "131")
public class BarbarianClass extends Card {

    public BarbarianClass() {
        addEffect(EffectSlot.STATIC, new RollWithAdvantageEffect());

        target(TargetFilters.creatureYouControl()).addEffect(EffectSlot.ON_CONTROLLER_ROLLS_ONE_OR_MORE_DICE,
                new ConditionalEffect(
                        new SourceCounterThreshold(1, CounterType.LEVEL),
                        SequenceEffect.of(
                                new BoostTargetCreatureEffect(2, 0),
                                new GrantKeywordEffect(Keyword.MENACE, GrantScope.TARGET))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new ClassLevelUpEffect(2)),
                "Gain the next level as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new NotCondition(new SourceCounterThreshold(1, CounterType.LEVEL)),
                "this Class is level 1"));

        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new SourceCounterThreshold(2, CounterType.LEVEL),
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.ALL_OWN_CREATURES)));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new ClassLevelUpEffect(3)),
                "Gain the next level as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(new AllOf(List.of(
                new SourceCounterThreshold(1, CounterType.LEVEL),
                new NotCondition(new SourceCounterThreshold(2, CounterType.LEVEL)))),
                "this Class is level 2"));
    }
}
