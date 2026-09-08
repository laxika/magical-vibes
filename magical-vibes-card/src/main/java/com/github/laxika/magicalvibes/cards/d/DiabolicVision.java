package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "ICE", collectorNumber = "284")
@CardRegistration(set = "BTD", collectorNumber = "67")
public class DiabolicVision extends Card {

    public DiabolicVision() {
        // Look at the top five cards of your library. Put one of them into your hand and the rest
        // on top of your library in any order.
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.chooseOneToHandRestOnTop(5));
    }
}
