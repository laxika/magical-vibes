package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBlockingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;

import java.util.List;

@CardRegistration(set = "TMC", collectorNumber = "26")
public class SpecialMove extends Card {

    public SpecialMove() {
        TargetFilter controlledCreature = new ControlledPermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature you control.");
        TargetFilter attackingOrBlockingCreature = new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsAttackingPredicate(),
                                new PermanentIsBlockingPredicate())))),
                "Target must be an attacking or blocking creature you control.");
        TargetFilter anyTarget = new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate())),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be any other target.");

        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Jump Kick — Destroy target artifact",
                        new DestroyTargetPermanentEffect(),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsArtifactPredicate(),
                                "Target must be an artifact.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Dash Attack — Put two +1/+1 counters on target attacking or blocking creature you control",
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 2),
                        attackingOrBlockingCreature),
                new ChooseOneEffect.ChooseOneOption(
                        "Foot Toss — Target creature you control deals damage equal to its power to any other target, then sacrifice it",
                        List.<CardEffect>of(
                                new TargetCreatureDealsPowerDamageToAnyTargetEffect(2, 1),
                                new SacrificeTargetCreatureEffect(2)),
                        List.of(anyTarget, controlledCreature))
        ), 2));
    }
}
