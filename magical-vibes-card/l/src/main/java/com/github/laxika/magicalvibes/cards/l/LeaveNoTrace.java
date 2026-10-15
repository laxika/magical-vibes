package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetAndOtherEnchantmentsSharingColorEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RAV", collectorNumber = "23")
public class LeaveNoTrace extends Card {

    public LeaveNoTrace() {
        target(TargetFilters.enchantment())
                .addEffect(EffectSlot.SPELL, new DestroyTargetAndOtherEnchantmentsSharingColorEffect());
    }
}
