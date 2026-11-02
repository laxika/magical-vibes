package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PTK", collectorNumber = "85")
public class WeiAmbushForce extends Card {

    public WeiAmbushForce() {
        // Whenever Wei Ambush Force attacks, it gets +2/+0 until end of turn.
        addTrigger(Triggers.attacks(), new BoostSelfEffect(2, 0));
    }
}
