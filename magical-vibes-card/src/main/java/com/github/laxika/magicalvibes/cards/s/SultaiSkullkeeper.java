package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "FRF", collectorNumber = "53")
public class SultaiSkullkeeper extends Card {

    public SultaiSkullkeeper() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(2, MillRecipient.CONTROLLER));
    }
}
