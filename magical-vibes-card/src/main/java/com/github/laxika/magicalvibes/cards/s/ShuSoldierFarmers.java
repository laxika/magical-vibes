package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "PTK", collectorNumber = "27")
public class ShuSoldierFarmers extends Card {

    public ShuSoldierFarmers() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(4));
    }
}
