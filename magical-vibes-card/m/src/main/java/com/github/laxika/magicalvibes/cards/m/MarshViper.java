package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "4ED", collectorNumber = "263")
@CardRegistration(set = "5ED", collectorNumber = "315")
@CardRegistration(set = "DRK", collectorNumber = "81")
public class MarshViper extends Card {

    public MarshViper() {
        // Whenever this creature deals damage to a player, that player gets two poison counters.
        addTrigger(Triggers.dealsDamageToPlayer(), new GivePoisonCountersEffect(2, PoisonRecipient.TARGET_PLAYER));
    }
}
