package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "IKO", collectorNumber = "150")
public class ExcavationMole extends Card {

    public ExcavationMole() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(3, MillRecipient.CONTROLLER));
    }
}
