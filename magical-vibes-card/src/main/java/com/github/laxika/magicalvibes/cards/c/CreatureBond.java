package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "4ED", collectorNumber = "66")
public class CreatureBond extends Card {

    public CreatureBond() {
        // Enchant creature. When enchanted creature dies, this Aura deals damage equal to that
        // creature's toughness to the creature's controller. The creature is gone by resolution, so
        // the death collector bakes its last-known toughness as the amount; ENCHANTED_PERMANENT_CONTROLLER
        // routes the damage to that creature's controller (baked as the stack targetId).
        target(TargetFilters.creature()).addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                new DealDamageToPlayersEffect(0, DamageRecipient.ENCHANTED_PERMANENT_CONTROLLER));
    }
}
