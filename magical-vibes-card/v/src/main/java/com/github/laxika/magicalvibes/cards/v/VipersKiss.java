package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantActivateAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "109")
public class VipersKiss extends Card {

    public VipersKiss() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(-1, -1, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new EnchantedCreatureCantActivateAbilitiesEffect());
    }
}
