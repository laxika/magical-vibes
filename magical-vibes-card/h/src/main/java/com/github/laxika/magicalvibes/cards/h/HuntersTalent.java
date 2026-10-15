package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "179")
public class HuntersTalent extends Card {

    public HuntersTalent() {
        target(TargetFilters.creatureYouControl());
        target(TargetFilters.creatureAnOpponentControls(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TargetDealsPowerDamageToTargetEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new PutCountersOnSelfEffect(CounterType.LEVEL)),
                "Level up {1}{G} ({1}{G}: Put a level counter on this. Level up only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new NotCondition(new SourceCounterThreshold(1, CounterType.LEVEL)),
                "This Class is already level 2 or higher."));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(new PutCountersOnSelfEffect(CounterType.LEVEL)),
                "Level up {3}{G} ({3}{G}: Put a level counter on this. Level up only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new AllOf(List.of(
                        new SourceCounterThreshold(1, CounterType.LEVEL),
                        new NotCondition(new SourceCounterThreshold(2, CounterType.LEVEL)))),
                "This Class must be level 2."));

        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                        new ConditionalEffect(
                                new SourceCounterThreshold(1, CounterType.LEVEL),
                                new BoostTargetCreatureEffect(1, 0)))
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                        new ConditionalEffect(
                                new SourceCounterThreshold(1, CounterType.LEVEL),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new ConditionalEffect(
                new ControlsPermanentCount(1, new PermanentPowerAtLeastPredicate(4)),
                new ConditionalEffect(
                        new SourceCounterThreshold(2, CounterType.LEVEL),
                        new DrawCardEffect(1))));
    }
}
