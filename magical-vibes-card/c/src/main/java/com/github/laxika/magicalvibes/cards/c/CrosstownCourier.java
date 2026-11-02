package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RTR", collectorNumber = "34")
@CardRegistration(set = "DDM", collectorNumber = "6")
public class CrosstownCourier extends Card {

    public CrosstownCourier() {
        // The damaged player mills cards equal to the combat damage dealt (bound as the event value).
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MillEffect(new EventValue(), MillRecipient.TARGET_PLAYER));
    }
}
