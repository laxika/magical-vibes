package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.effect.AddClueTokenToTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.SolveSourceEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsSolved;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "9")
public class CaseOfThePilferedProof extends Card {

    public CaseOfThePilferedProof() {
        addTrigger(Triggers.enters(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.DETECTIVE))),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new PutCountersOnEnteringCreatureEffect(1, false));

        addTrigger(Triggers.turnedFaceUp(
                        new PermanentAllOfPredicate(List.of(
                                Triggers.CREATURE,
                                new PermanentHasSubtypePredicate(CardSubtype.DETECTIVE))),
                        ObjectRelation.YOU_CONTROL),
                new PutCounterOnReferencedPermanentEffect(
                        PermanentReference.TRIGGERING, CounterType.PLUS_ONE_PLUS_ONE));

        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AllOf(List.of(
                                        new ControlsPermanentCount(3,
                                                new PermanentHasSubtypePredicate(CardSubtype.DETECTIVE)),
                                        new NotCondition(new SourceIsSolved())
                                ))),
                new SolveSourceEffect());

        addStaticEffect(new AddClueTokenToTokenCreationEffect());
    }
}
