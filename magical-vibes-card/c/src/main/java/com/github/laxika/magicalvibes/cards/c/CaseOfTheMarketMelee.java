package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsSolved;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageNotRemovedDuringCleanupEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SolveSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsDamagedPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "YMKM", collectorNumber = "13")
public class CaseOfTheMarketMelee extends Card {

    public CaseOfTheMarketMelee() {
        target(new AnyTargetPredicateTargetFilter(
                TargetPredicates.anyTarget().permanentRestriction().orElseThrow(),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be any target"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToAnyTargetEffect(1));

        addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                new DamageNotRemovedDuringCleanupEffect(), GrantScope.ALL_CREATURES_INCLUDING_SELF));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(new AllOf(List.of(
                        new AnyPlayerControlsPermanentCount(3, new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsDamagedPredicate()))),
                        new NotCondition(new SourceIsSolved())
                )), new SolveSourceEffect()));

        target(new AnyTargetPredicateTargetFilter(
                TargetPredicates.anyTarget().permanentRestriction().orElseThrow(),
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be any target"),
                0, Integer.MAX_VALUE).addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                new ConditionalEffect(new SourceIsSolved(),
                        DealDividedDamageEffect.chosenAmongAnyTargetsAtResolution(new XValue())));
    }
}
