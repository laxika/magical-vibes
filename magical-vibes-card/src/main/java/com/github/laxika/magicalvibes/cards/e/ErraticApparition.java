package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "DSK", collectorNumber = "54")
public class ErraticApparition extends Card {

    public ErraticApparition() {
        BoostSelfEffect boost = new BoostSelfEffect(1, 1);
        addEffect(EffectSlot.ON_ALLY_ENCHANTMENT_ENTERS_BATTLEFIELD, boost);
        addEffect(EffectSlot.ON_ALLY_ROOM_FULLY_UNLOCKED, boost);
    }
}
