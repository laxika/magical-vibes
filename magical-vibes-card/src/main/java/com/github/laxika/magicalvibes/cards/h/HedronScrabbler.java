package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "ZEN", collectorNumber = "204")
public class HedronScrabbler extends Card {

    public HedronScrabbler() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new BoostSelfEffect(1, 1));
    }
}
