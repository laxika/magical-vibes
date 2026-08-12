package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "ZEN", collectorNumber = "188")
public class TerritorialBaloth extends Card {

    public TerritorialBaloth() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new BoostSelfEffect(2, 2));
    }
}
