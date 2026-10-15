package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "PTK", collectorNumber = "85")
public class WeiAmbushForce extends Card {

    public WeiAmbushForce() {
        // Whenever Wei Ambush Force attacks, it gets +2/+0 until end of turn.
        addEffect(EffectSlot.ON_ATTACK, new BoostSelfEffect(2, 0));
    }
}
