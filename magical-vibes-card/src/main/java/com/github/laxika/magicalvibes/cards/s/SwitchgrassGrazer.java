package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsSaddled;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DamageNotRemovedDuringCleanupEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetThenPerpetuallyGrantStaticEffectsIfCreatureDamagedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SaddleCost;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "YOTJ", collectorNumber = "14")
public class SwitchgrassGrazer extends Card {

    public SwitchgrassGrazer() {
        target(new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentIsBattlePredicate())),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be any target"))
                .addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                        new SourceIsSaddled(),
                        new DealDamageToAnyTargetThenPerpetuallyGrantStaticEffectsIfCreatureDamagedEffect(
                                1, List.of(new CantBlockEffect(), new DamageNotRemovedDuringCleanupEffect()))));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SaddleCost(1), new BecomeSaddledUntilEndOfTurnEffect(GrantScope.SELF)),
                "Saddle 1",
                ActivationTimingRestriction.SORCERY_SPEED));
    }
}
