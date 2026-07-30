package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToBattlefieldOnDeathEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AVR", collectorNumber = "124")
public class UnhallowedPact extends Card {

    public UnhallowedPact() {
        // Enchant creature
        target(TargetFilters.creature());

        // When enchanted creature dies, return that card to the battlefield under your control.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                new ReturnEnchantedCreatureToBattlefieldOnDeathEffect(true));
    }
}
