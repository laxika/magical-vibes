package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RNA", collectorNumber = "54")
public class Slimebind extends Card {

    public Slimebind() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(-4, 0, GrantScope.ENCHANTED_CREATURE));
    }
}
