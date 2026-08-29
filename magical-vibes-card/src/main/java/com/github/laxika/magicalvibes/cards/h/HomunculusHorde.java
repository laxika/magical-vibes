package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;

@CardRegistration(set = "FDN", collectorNumber = "41")
public class HomunculusHorde extends Card {

    public HomunculusHorde() {
        addEffect(EffectSlot.ON_CONTROLLER_DRAWS_SECOND_CARD, new CreateTokenCopyOfSourceEffect());
    }
}
