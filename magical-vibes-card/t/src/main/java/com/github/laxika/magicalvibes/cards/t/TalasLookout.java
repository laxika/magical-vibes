package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "DMU", collectorNumber = "68")
public class TalasLookout extends Card {

    public TalasLookout() {
        addEffect(EffectSlot.ON_DEATH, LookAtTopCardsEffect.chooseNToHandRestToGraveyard(2, 1));
    }
}
