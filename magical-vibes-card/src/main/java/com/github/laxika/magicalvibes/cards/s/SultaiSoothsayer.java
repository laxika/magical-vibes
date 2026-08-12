package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "KTK", collectorNumber = "205")
public class SultaiSoothsayer extends Card {

    public SultaiSoothsayer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                LookAtTopCardsEffect.chooseNToHandRestToGraveyard(4, 1));
    }
}
