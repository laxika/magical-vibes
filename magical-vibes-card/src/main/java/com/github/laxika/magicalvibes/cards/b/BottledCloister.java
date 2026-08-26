package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileHandFaceDownWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutAllCardsExiledWithSourceIntoOwnersHandsEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "RAV", collectorNumber = "256")
public class BottledCloister extends Card {

    public BottledCloister() {
        addEffect(EffectSlot.OPPONENT_UPKEEP_TRIGGERED, new ExileHandFaceDownWithSourceEffect());
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new PutAllCardsExiledWithSourceIntoOwnersHandsEffect(true),
                new DrawCardEffect()));
    }
}
