package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsAndSeparateEffect;

@CardRegistration(set = "HOB", collectorNumber = "53")
public class RiddlesInTheDark extends Card {

    public RiddlesInTheDark() {
        addEffect(EffectSlot.SPELL,
                new RevealTopCardsAndSeparateEffect(4, CardPileDisposition.HAND_WITH_FACE_DOWN_PILE, true));
    }
}
