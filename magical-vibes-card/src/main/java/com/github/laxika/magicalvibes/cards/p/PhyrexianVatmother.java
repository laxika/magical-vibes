package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GiveControllerPoisonCountersEffect;

@CardRegistration(set = "MBS", collectorNumber = "52")
public class PhyrexianVatmother extends Card {

    public PhyrexianVatmother() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new GiveControllerPoisonCountersEffect(1));
    }
}
