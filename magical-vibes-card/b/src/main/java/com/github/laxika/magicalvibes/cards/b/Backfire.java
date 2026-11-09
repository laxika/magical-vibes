package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "4ED", collectorNumber = "62")
@CardRegistration(set = "LEG", collectorNumber = "47")
public class Backfire extends Card {

    public Backfire() {
        target(TargetFilters.creature())
                // Whenever enchanted creature deals damage to you, this Aura deals that much
                // damage to that creature's controller.
                .addTrigger(Triggers.enchantedCreatureDealsDamageToYou(),
                        new DealDamageToPlayersEffect(new EventValue(), DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
