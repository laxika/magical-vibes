package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsDrawnThisResolution;
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
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "225")
public class ObliviousBookworm extends Card {

    public ObliviousBookworm() {
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new MayEffect(
                        SequenceEffect.of(
                                new DrawCardEffect(),
                                new ConditionalEffect(
                                        new NotCondition(new AnyOf(List.of(
                                                new FaceDownPermanentEnteredThisTurn(),
                                                new PermanentTurnedFaceUpThisTurn()))),
                                        new DiscardEffect(new CardsDrawnThisResolution(), DiscardRecipient.CONTROLLER))),
                        "Draw a card?"));
    }
}
