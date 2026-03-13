package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "36")
public class TirelessMissionaries extends Card {

    public TirelessMissionaries() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(3));
    }
}
