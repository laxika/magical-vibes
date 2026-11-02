package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "GTC", collectorNumber = "121")
@CardRegistration(set = "SLD", collectorNumber = "1415")
@CardRegistration(set = "NCC", collectorNumber = "293")
@CardRegistration(set = "DSC", collectorNumber = "179")
@CardRegistration(set = "C19", collectorNumber = "169")
public class GiantAdephage extends Card {

    public GiantAdephage() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new CreateTokenCopyOfSourceEffect());
    }
}
