package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "ORI", collectorNumber = "116")
@CardRegistration(set = "THS", collectorNumber = "103")
public class ReturnedCentaur extends Card {

    public ReturnedCentaur() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(4, MillRecipient.TARGET_PLAYER));
    }
}
