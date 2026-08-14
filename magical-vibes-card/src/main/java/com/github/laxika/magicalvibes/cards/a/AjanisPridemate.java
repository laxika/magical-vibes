package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "M11", collectorNumber = "3")
@CardRegistration(set = "M15", collectorNumber = "2")
@CardRegistration(set = "M19", collectorNumber = "5")
@CardRegistration(set = "FDN", collectorNumber = "135")
public class AjanisPridemate extends Card {

    public AjanisPridemate() {
        addEffect(EffectSlot.ON_CONTROLLER_GAINS_LIFE, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
