package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "APC", collectorNumber = "99")
public class FlowstoneCharger extends Card {

    public FlowstoneCharger() {
        addTrigger(Triggers.attacks(), BoostEffect.self(3, -3));
    }
}
