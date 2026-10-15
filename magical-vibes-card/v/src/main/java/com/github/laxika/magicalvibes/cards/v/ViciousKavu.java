package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "INV", collectorNumber = "284")
public class ViciousKavu extends Card {

    public ViciousKavu() {
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(2, 0));
    }
}
