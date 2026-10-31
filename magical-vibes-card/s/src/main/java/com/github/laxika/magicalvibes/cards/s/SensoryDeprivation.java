package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ISD", collectorNumber = "74")
@CardRegistration(set = "M14", collectorNumber = "71")
public class SensoryDeprivation extends Card {

    public SensoryDeprivation() {
        target(TargetFilters.creature()).addStaticEffect(new StaticBoostEffect(-3, 0, GrantScope.ENCHANTED_CREATURE));
    }
}
