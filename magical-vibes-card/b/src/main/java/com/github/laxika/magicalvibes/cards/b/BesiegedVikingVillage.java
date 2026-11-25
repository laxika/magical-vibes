package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.DidntAttack;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAttackedThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WHO", collectorNumber = "570")
public class BesiegedVikingVillage extends Card {

    public BesiegedVikingVillage() {
        addStaticEffect(new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        false,
                        "{1}",
                        List.of(PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)),
                        "Boast — {1}: Put a +1/+1 counter on this creature. Activate only if this creature "
                                + "attacked this turn and only once each turn.",
                        1
                ).withActivationCondition(
                        new NotCondition(new DidntAttack()),
                        "Activate only if this creature attacked this turn."
                ).withBoast(),
                Subjects.otherCreatures()
        ));

        PermanentPredicate targetCreatureYouControlThatAttacked = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentAttackedThisTurnPredicate()
        ));
        target(new PermanentPredicateTargetFilter(
                targetCreatureYouControlThatAttacked,
                "Target must be a creature you control that attacked this turn"
        )).addTrigger(Triggers.chaosEnsues(),
                PutCountersEffect.withTargetRestriction(
                        CounterType.INDESTRUCTIBLE, 1, targetCreatureYouControlThatAttacked));
    }
}
