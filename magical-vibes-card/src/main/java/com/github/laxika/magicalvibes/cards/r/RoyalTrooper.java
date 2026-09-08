package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "S99", collectorNumber = "25")
public class RoyalTrooper extends Card {

    public RoyalTrooper() {
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfEffect(2, 2));
    }
}
