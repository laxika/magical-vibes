package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "32")
@CardRegistration(set = "IMA", collectorNumber = "32")
public class StalwartAven extends Card {

    public StalwartAven() {
        // Renown 1
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new RenownEffect(1));
    }
}
