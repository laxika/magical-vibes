package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TargetingRestrictionEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ICE", collectorNumber = "302")
public class SpectralShield extends Card {

    public SpectralShield() {
        target(TargetFilters.creature())
        // Enchanted creature gets +0/+2.
        .addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 2, GrantScope.ENCHANTED_CREATURE))
        // Enchanted creature can't be the target of spells (abilities can still target it).
        .addEffect(EffectSlot.STATIC,
                new GrantEffectEffect(TargetingRestrictionEffect.spells(), GrantScope.ENCHANTED_CREATURE));
    }
}
