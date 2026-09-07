package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlledCreaturesTotalPowerAtLeast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsSolved;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SolveSourceEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "156")
public class CaseOfTheTrampledGarden extends Card {

    public CaseOfTheTrampledGarden() {
        target(TargetFilters.creatureYouControl(), 1, 2)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        DistributeCountersAmongTargetsEffect.evenlyAmongTargets(
                                CounterType.PLUS_ONE_PLUS_ONE, 2));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new AllOf(List.of(
                        new ControlledCreaturesTotalPowerAtLeast(8),
                        new NotCondition(new SourceIsSolved())
                )), new SolveSourceEffect()));

        target(TargetFilters.attackingCreature())
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                        new ConditionalEffect(new SourceIsSolved(), SequenceEffect.of(
                                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET))));
    }
}
