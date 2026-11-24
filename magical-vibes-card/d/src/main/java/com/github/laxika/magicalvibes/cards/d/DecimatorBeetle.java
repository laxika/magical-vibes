package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveUpToCountersFromAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "197")
public class DecimatorBeetle extends Card {

    public DecimatorBeetle() {
        // "When this creature enters, put a -1/-1 counter on target creature you control."
        target(TargetFilters.creatureYouControl()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1));

        // "Whenever this creature attacks, remove a -1/-1 counter from target creature you control and put a -1/-1
        // counter on up to one target creature defending player controls." Each half acts on its own target, so one
        // still happens when the other's target is illegal.
        CardEffect removeCounter = RemoveUpToCountersFromAllPermanentsEffect.exactly(
                CounterType.MINUS_ONE_MINUS_ONE, new Fixed(1), Subjects.target());
        CardEffect putCounter = new PutCounterOnTargetPermanentEffect(CounterType.MINUS_ONE_MINUS_ONE, 1);
        SpellTarget yourCreature = target(TargetFilters.creatureYouControl());
        SpellTarget defendingCreature = target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledByDefendingPlayerPredicate())),
                "Target must be a creature defending player controls"), 0, 1);
        addTrigger(Triggers.attacks(), removeCounter, putCounter);
        registerEffectTargetIndex(removeCounter, yourCreature.getIndex());
        registerEffectTargetIndex(putCounter, defendingCreature.getIndex());
    }
}
