package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "THS", collectorNumber = "79")
public class BloodTollHarpy extends Card {

    public BloodTollHarpy() {
        // When this creature enters, each player loses 1 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(1, LoseLifeRecipient.EACH_PLAYER));
    }
}
