package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceClassLevelAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ClassLevelUpEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.TargetDealsPowerDamageToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "179")
public class HuntersTalent extends Card {

    public HuntersTalent() {
        target(TargetFilters.creatureYouControl());
        target(TargetFilters.creatureAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new TargetDealsPowerDamageToTargetEffect());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}",
                List.of(new ClassLevelUpEffect(2)),
                "{1}{G}: Level 2",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new NotCondition(new SourceClassLevelAtLeast(2)),
                "This Class is already level 2 or higher."));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(new ClassLevelUpEffect(3)),
                "{3}{G}: Level 3",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(
                new AllOf(List.of(
                        new SourceClassLevelAtLeast(2),
                        new NotCondition(new SourceClassLevelAtLeast(3)))),
                "This Class must be level 2."));

        target(TargetFilters.attackingCreature())
                .addTrigger(Triggers.youAttack(),
                        ConditionalEffect.atTriggerTime(
                                new SourceClassLevelAtLeast(2),
                                new BoostTargetCreatureEffect(1, 0)))
                .addTrigger(Triggers.youAttack(),
                        ConditionalEffect.atTriggerTime(
                                new SourceClassLevelAtLeast(2),
                                new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.TARGET)));

        addTrigger(Triggers.endStep(ActivePlayer.YOU), new ConditionalEffect(
                new AllOf(List.of(
                        new SourceClassLevelAtLeast(3),
                        new ControlsPermanentCount(1, new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentPowerAtLeastPredicate(4)))))),
                new DrawCardEffect(1)));
    }
}
