package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "DST", collectorNumber = "44")
public class GreaterHarvester extends Card {

    public GreaterHarvester() {
        // At the beginning of your upkeep, sacrifice a permanent.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new SacrificePermanentsEffect(1, new PermanentTruePredicate(), SacrificeRecipient.CONTROLLER));

        // Whenever this creature deals combat damage to a player, that player sacrifices two permanents of their choice.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new SacrificePermanentsEffect(2, new PermanentTruePredicate(), SacrificeRecipient.TARGET_PLAYER));
    }
}
