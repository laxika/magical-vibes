package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "6ED", collectorNumber = "91")
@CardRegistration(set = "5ED", collectorNumber = "113")
@CardRegistration(set = "4ED", collectorNumber = "96")
@CardRegistration(set = "SUM", collectorNumber = "76")
public class PsychicVenom extends Card {

    public PsychicVenom() {
        target(TargetFilters.land());
        // Whenever enchanted land becomes tapped, this Aura deals 2 damage to that land's controller.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED,
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PERMANENT_CONTROLLER));
    }
}
