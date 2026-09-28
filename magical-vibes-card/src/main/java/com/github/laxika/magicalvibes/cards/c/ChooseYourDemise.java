package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsAndSeparateEffect;

@CardRegistration(set = "DSC", collectorNumber = "331")
public class ChooseYourDemise extends Card {

    public ChooseYourDemise() {
        addEffect(EffectSlot.SPELL, new RevealTopCardsAndSeparateEffect(
                4, CardPileDisposition.HAND_AND_BOTTOM_WITH_FACE_DOWN_PILE, true));
    }
}
