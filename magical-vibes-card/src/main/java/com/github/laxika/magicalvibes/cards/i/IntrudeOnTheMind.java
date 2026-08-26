package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsAndSeparateEffect;

@CardRegistration(set = "MKM", collectorNumber = "61")
public class IntrudeOnTheMind extends Card {

    public IntrudeOnTheMind() {
        addEffect(EffectSlot.SPELL, new RevealTopCardsAndSeparateEffect(
                5, CardPileDisposition.HAND_AND_THOPTER, true));
    }
}
