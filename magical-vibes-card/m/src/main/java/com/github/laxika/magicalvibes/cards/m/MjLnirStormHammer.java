package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ACR", collectorNumber = "74")
public class MjLnirStormHammer extends Card {

    public MjLnirStormHammer() {
        PermanentAllOfPredicate legendaryCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)));

        target(new ControlledPermanentPredicateTargetFilter(
                legendaryCreature, "Target must be a legendary creature you control"))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new AttachSourceEquipmentToTargetCreatureEffect());

        PermanentAllOfPredicate tappedCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentIsTappedPredicate()));
        PermanentPredicateTargetFilter defendingCreature = new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledByDefendingPlayerPredicate())),
                "Target must be a creature defending player controls");

        target(defendingCreature).addTrigger(Triggers.attacks(), SequenceEffect.of(
                new TapPermanentsEffect(Subjects.target()),
                new PutCountersEffect(CounterType.STUN),
                DealDamageEffect.eachEvaluatedFor(new PermanentCount(tappedCreature, CountScope.CONTROLLER),
                        Subjects.eachOpponent())));

        addActivatedAbility(new EquipActivatedAbility("{4}"));
    }
}
