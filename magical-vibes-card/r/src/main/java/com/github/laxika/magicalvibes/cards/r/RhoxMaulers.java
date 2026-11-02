package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "196")
public class RhoxMaulers extends Card {

    public RhoxMaulers() {
        // Renown 2 (Trample comes from the Scryfall-loaded keywords.)
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new RenownEffect(2));
    }
}
