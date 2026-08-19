package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PCY", collectorNumber = "67")
public class GreelsCaress extends Card {

    public GreelsCaress() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(-3, 0, GrantScope.ENCHANTED_CREATURE));
    }
}
