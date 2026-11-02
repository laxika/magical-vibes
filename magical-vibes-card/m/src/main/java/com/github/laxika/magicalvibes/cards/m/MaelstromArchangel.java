package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CON", collectorNumber = "115")
@CardRegistration(set = "DMC", collectorNumber = "158")
public class MaelstromArchangel extends Card {

    public MaelstromArchangel() {
        // Flying is loaded from Scryfall.
        // Whenever this creature deals combat damage to a player, you may cast a spell from your
        // hand without paying its mana cost.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayCastAnySpellFromHandWithoutPayingManaCostEffect());
    }
}
