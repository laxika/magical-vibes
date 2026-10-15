package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsMotivated;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MotivateEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MB1", collectorNumber = "56")
public class LazierGoblin extends Card {

    public LazierGoblin() {
        addEffect(EffectSlot.STATIC, new CantAttackOrBlockUnlessEffect(
                new SourceIsMotivated(), "you have paid its motivate cost once"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(new MotivateEffect()),
                "Motivate {3}{R} ({3}{R}: Motivate this creature. Motivate only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        target(new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()
                )),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a creature, planeswalker, or player"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToAnyTargetEffect(2));
    }
}
