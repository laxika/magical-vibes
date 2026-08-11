package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "ECL", collectorNumber = "118")
public class ScarbladeScout extends Card {

    public ScarbladeScout() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MillEffect(2, MillRecipient.CONTROLLER));
    }
}
