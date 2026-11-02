package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayWhileSourceControlledEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "462")
@CardRegistration(set = "FIN", collectorNumber = "560")
public class LightningSecuritySergeant extends Card {

    public LightningSecuritySergeant() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ExileTopCardMayPlayWhileSourceControlledEffect());
    }
}
