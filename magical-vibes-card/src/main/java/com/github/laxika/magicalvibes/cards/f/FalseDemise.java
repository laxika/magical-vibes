package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToBattlefieldOnDeathEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ALL", collectorNumber = "27a")
@CardRegistration(set = "ALL", collectorNumber = "27b")
public class FalseDemise extends Card {

    public FalseDemise() {
        // Enchant creature
        target(TargetFilters.creature());

        // When enchanted creature dies, return that card to the battlefield under your control.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD,
                new ReturnEnchantedCreatureToBattlefieldOnDeathEffect(true));
    }
}
