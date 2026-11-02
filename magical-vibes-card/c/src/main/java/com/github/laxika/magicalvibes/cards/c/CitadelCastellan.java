package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "213")
public class CitadelCastellan extends Card {

    public CitadelCastellan() {
        // Renown 2
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new RenownEffect(2));
    }
}
