package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AKH", collectorNumber = "187")
@CardRegistration(set = "AKR", collectorNumber = "220")
public class SixthSense extends Card {

    public SixthSense() {
        target(TargetFilters.creature())
                // Whenever enchanted creature deals combat damage to a player, you may draw a card.
                .addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
