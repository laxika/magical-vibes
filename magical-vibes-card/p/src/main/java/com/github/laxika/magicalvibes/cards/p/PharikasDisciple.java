package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "194")
public class PharikasDisciple extends Card {

    public PharikasDisciple() {
        // Renown 1 (Deathtouch comes from the Scryfall-loaded keywords.)
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new RenownEffect(1));
    }
}
