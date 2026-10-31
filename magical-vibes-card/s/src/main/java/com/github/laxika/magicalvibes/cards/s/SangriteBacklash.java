package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ARB", collectorNumber = "139")
public class SangriteBacklash extends Card {

    public SangriteBacklash() {
        target(TargetFilters.creature()).addStaticEffect(new StaticBoostEffect(3, -3, GrantScope.ENCHANTED_CREATURE));
    }
}
