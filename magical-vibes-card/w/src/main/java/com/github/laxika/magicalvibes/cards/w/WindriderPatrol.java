package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "89")
@CardRegistration(set = "PIO", collectorNumber = "80")
public class WindriderPatrol extends Card {

    public WindriderPatrol() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new ScryEffect(2));
    }
}
