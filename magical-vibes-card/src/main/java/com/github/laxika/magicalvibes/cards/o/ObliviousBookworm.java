package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.FaceDownPermanentEnteredThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.PermanentTurnedFaceUpThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "225")
public class ObliviousBookworm extends Card {

    public ObliviousBookworm() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new MayEffect(
                        SequenceEffect.of(
                                new DrawCardEffect(),
                                ConditionalEffect.unless(
                                        new NotCondition(new AnyOf(List.of(
                                                new FaceDownPermanentEnteredThisTurn(),
                                                new PermanentTurnedFaceUpThisTurn()))),
                                        new DiscardEffect(1, DiscardRecipient.CONTROLLER))),
                        "Draw a card?"));
    }
}
