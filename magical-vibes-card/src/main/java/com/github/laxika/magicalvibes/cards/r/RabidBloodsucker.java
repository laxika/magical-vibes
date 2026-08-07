package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "ORI", collectorNumber = "113")
public class RabidBloodsucker extends Card {

    public RabidBloodsucker() {
        // When this creature enters, each player loses 2 life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(2, LoseLifeRecipient.EACH_PLAYER));
    }
}
