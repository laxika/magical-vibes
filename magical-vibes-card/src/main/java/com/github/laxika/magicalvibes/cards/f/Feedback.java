package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "5ED", collectorNumber = "85")
@CardRegistration(set = "4ED", collectorNumber = "71")
@CardRegistration(set = "ITP", collectorNumber = "9")
@CardRegistration(set = "RQS", collectorNumber = "9")
public class Feedback extends Card {

    public Feedback() {
        // Enchant enchantment. At the beginning of the upkeep of enchanted enchantment's
        // controller, this Aura deals 1 damage to that player.
        target(TargetFilters.enchantment()).addEffect(EffectSlot.ENCHANTED_PERMANENT_CONTROLLER_UPKEEP_TRIGGERED,
                new DealDamageToPlayersEffect(1, DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER));
    }
}
