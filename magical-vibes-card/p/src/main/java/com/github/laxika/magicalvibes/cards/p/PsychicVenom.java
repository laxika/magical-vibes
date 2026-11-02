package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "6ED", collectorNumber = "91")
@CardRegistration(set = "5ED", collectorNumber = "113")
@CardRegistration(set = "4ED", collectorNumber = "96")
@CardRegistration(set = "SUM", collectorNumber = "76")
@CardRegistration(set = "ME1", collectorNumber = "46")
@CardRegistration(set = "3ED", collectorNumber = "76")
@CardRegistration(set = "2ED", collectorNumber = "76")
@CardRegistration(set = "LEA", collectorNumber = "75")
@CardRegistration(set = "LEB", collectorNumber = "76")
public class PsychicVenom extends Card {

    public PsychicVenom() {
        target(TargetFilters.land());
        // Whenever enchanted land becomes tapped, this Aura deals 2 damage to that land's controller.
        addTrigger(Triggers.enchantedPermanentBecomesTapped(),
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
