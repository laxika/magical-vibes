package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.NotControllerTurn;
import com.github.laxika.magicalvibes.model.condition.NthAbilityResolutionThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/** Back face of Prowl, Stoic Strategist. */
public class ProwlPursuitVehicle extends Card {

    public ProwlPursuitVehicle() {
        // Living metal: during your turn, this Vehicle is also a creature.
        addStaticEffect(new ConditionalEffect(
                new NotCondition(new NotControllerTurn()),
                new GrantCardTypeEffect(CardType.CREATURE, Subjects.source())));

        PermanentPredicate anotherCreatureOrVehicle = new PermanentAllOfPredicate(List.of(
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)))));
        addTrigger(Triggers.enters(anotherCreatureOrVehicle, ObjectRelation.ANOTHER_YOU_CONTROL),
                SequenceEffect.of(
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                        new ConditionalEffect(new NthAbilityResolutionThisTurn(2),
                                new TransformSelfEffect())));
    }
}
