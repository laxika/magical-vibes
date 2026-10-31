package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GS1", collectorNumber = "32")
public class HardenedScaleArmor extends Card {

    public HardenedScaleArmor() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(3, 3, GrantScope.ENCHANTED_CREATURE));
    }
}
