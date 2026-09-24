package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsTapped;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SeekEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SurvivalTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueAtMostControlledTappedCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;

import java.util.List;

@CardRegistration(set = "YDSK", collectorNumber = "19")
public class EffieFastLearner extends Card {

    public EffieFastLearner() {
        addEffect(EffectSlot.POSTCOMBAT_MAIN_TRIGGERED,
                new SurvivalTriggerEffect(new ConditionalEffect(
                        new SourceIsTapped(),
                        SequenceEffect.of(
                                new PutCounterOnEachControlledPermanentEffect(
                                        CounterType.PLUS_ONE_PLUS_ONE,
                                        1,
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentIsCreaturePredicate(),
                                                new PermanentIsTappedPredicate()))),
                                new SeekEffect(new CardAllOfPredicate(List.of(
                                        new CardSubtypePredicate(CardSubtype.SURVIVOR),
                                        new CardManaValueAtMostControlledTappedCreaturesPredicate())))))));
    }
}
