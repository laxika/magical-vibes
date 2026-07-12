package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "8ED", collectorNumber = "142")
public class MaggotCarrier extends Card {

    public MaggotCarrier() {
        // When this creature enters, each player loses 1 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(1, LoseLifeRecipient.EACH_PLAYER));
    }
}
