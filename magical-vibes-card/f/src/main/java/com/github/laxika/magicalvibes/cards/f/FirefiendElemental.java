package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RenownEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "146")
public class FirefiendElemental extends Card {

    public FirefiendElemental() {
        // Renown 1 (haste is a Scryfall-loaded keyword)
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new RenownEffect(1));
    }
}
