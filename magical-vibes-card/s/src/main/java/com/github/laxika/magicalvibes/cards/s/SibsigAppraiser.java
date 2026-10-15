package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "TDM", collectorNumber = "56")
public class SibsigAppraiser extends Card {

    public SibsigAppraiser() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, LookAtTopCardsEffect.chooseNToHandRestToGraveyard(2, 1));
    }
}
