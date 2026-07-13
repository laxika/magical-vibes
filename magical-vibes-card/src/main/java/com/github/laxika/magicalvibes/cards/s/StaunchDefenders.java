package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "8ED", collectorNumber = "49")
@CardRegistration(set = "7ED", collectorNumber = "50")
@CardRegistration(set = "6ED", collectorNumber = "45")
public class StaunchDefenders extends Card {

    public StaunchDefenders() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(4));
    }
}
