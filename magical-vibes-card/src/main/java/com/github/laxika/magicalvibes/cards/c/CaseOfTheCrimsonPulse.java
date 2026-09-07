package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.ControllerHandEmpty;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceIsSolved;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SolveSourceEffect;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "114")
public class CaseOfTheCrimsonPulse extends Card {

    public CaseOfTheCrimsonPulse() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                SequenceEffect.of(
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER),
                        new DrawCardEffect(2)));

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ConditionalEffect(
                        new AllOf(List.of(
                                new ControllerHandEmpty(),
                                new NotCondition(new SourceIsSolved())
                        )),
                        new SolveSourceEffect()));

        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new ConditionalEffect(
                        new SourceIsSolved(),
                        new DiscardOwnHandThenDrawEffect(new Fixed(2))));
    }
}
