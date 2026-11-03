package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AnyPlayerControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsSolved;
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
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YMKM", collectorNumber = "13")
public class CaseOfTheMarketMelee extends Card {

    public CaseOfTheMarketMelee() {
        target(new AnyTargetPredicateTargetFilter(
                TargetPredicates.anyTarget().permanentRestriction().orElseThrow(),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be any target"))
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new DealDamageToAnyTargetEffect(1));

        addStaticEffect(new GrantEffectEffect(
                new DamageNotRemovedDuringCleanupEffect(), GrantScope.ALL_CREATURES_INCLUDING_SELF));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AllOf(List.of(
                                        new AnyPlayerControlsPermanentCount(3, new PermanentAllOfPredicate(List.of(
                                                new PermanentIsCreaturePredicate(),
                                                new PermanentIsDamagedPredicate()))),
                                        new NotCondition(new SourceIsSolved())
                                ))),
                new SolveSourceEffect());

        target(new AnyTargetPredicateTargetFilter(
                TargetPredicates.anyTarget().permanentRestriction().orElseThrow(),
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be any target"),
                0, Integer.MAX_VALUE).addTrigger(Triggers.youAttack(),
                        TriggerOptions.DEFAULT.withInterveningIf(new SourceIsSolved()),
                        DealDividedDamageEffect.chosenAmongAnyTargetsAtResolution(new XValue()));
    }
}
