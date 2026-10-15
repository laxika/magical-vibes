package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "THS", collectorNumber = "130")
public class MinotaurSkullcleaver extends Card {

    public MinotaurSkullcleaver() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BoostSelfEffect(2, 0));
    }
}
