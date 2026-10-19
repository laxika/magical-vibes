package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceEffect;

@CardRegistration(set = "YWOE", collectorNumber = "28")
public class ThirdLittlePig extends Card {

    public ThirdLittlePig() {
        addEffect(EffectSlot.ON_CONTROLLER_CONJURES, new PerpetuallyBoostSourceEffect(1, 1));
        addEffect(EffectSlot.GRAVEYARD_ON_CONTROLLER_CONJURES, new PerpetuallyBoostSourceEffect(1, 1));
    }
}
