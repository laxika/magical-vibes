package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.effect.AddClueTokenToTokenCreationEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.SolveSourceEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsSolved;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "9")
public class CaseOfThePilferedProof extends Card {

    public CaseOfThePilferedProof() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.DETECTIVE),
                        new PutCountersOnEnteringCreatureEffect(1, false)));

        addEffect(EffectSlot.ON_SELF_OR_ALLY_CREATURE_TURNS_FACE_UP,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.DETECTIVE),
                        new PutCounterOnReferencedPermanentEffect(
                                PermanentReference.TRIGGERING, CounterType.PLUS_ONE_PLUS_ONE)));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(
                        new AllOf(List.of(
                                new ControlsPermanentCount(3,
                                        new PermanentHasSubtypePredicate(CardSubtype.DETECTIVE)),
                                new NotCondition(new SourceIsSolved())
                        )), new SolveSourceEffect()));

        addEffect(EffectSlot.STATIC, new AddClueTokenToTokenCreationEffect());
    }
}
