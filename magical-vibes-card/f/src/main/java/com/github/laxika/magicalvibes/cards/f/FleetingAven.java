package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "83")
public class FleetingAven extends Card {

    public FleetingAven() {
        addTrigger(Triggers.playerCyclesCard(), ReturnToHandEffect.self());
    }
}
