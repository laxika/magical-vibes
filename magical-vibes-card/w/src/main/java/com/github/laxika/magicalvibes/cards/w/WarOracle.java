package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "41")
@CardRegistration(set = "PIO", collectorNumber = "42")
public class WarOracle extends Card {

    public WarOracle() {
        // Renown 1
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new RenownEffect(1));
    }
}
