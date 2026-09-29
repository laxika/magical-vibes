package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCounterSum;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsMonstrous;
import com.github.laxika.magicalvibes.model.effect.MonstrosityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "FIC", collectorNumber = "68")
@CardRegistration(set = "FIC", collectorNumber = "160")
public class MaesterSeymour extends Card {

    public MaesterSeymour() {
        PermanentAllOfPredicate anotherCreatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        target(new PermanentPredicateTargetFilter(anotherCreatureYouControl,
                "Target must be another creature you control"))
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE,
                                new SourcePower()));

        SourceIsMonstrous monstrous = new SourceIsMonstrous();
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}{G}",
                List.of(new MonstrosityEffect(new PermanentCounterSum(
                        null, new PermanentIsCreaturePredicate(), CountScope.CONTROLLER))),
                "{3}{G}{G}: Monstrosity X, where X is the number of counters among creatures you control."
        ).withActivationCondition(new NotCondition(monstrous), "This creature is already monstrous"));
    }
}
