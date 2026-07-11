package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "PTK", collectorNumber = "20")
public class ShuDefender extends Card {

    public ShuDefender() {
        addEffect(EffectSlot.ON_BLOCK, new BoostSelfEffect(0, 2));
    }
}
