package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BLB", collectorNumber = "21")
public class MabelsMettle extends Card {

    public MabelsMettle() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 2));
        target(TargetFilters.creature(), 0, 1)
                .addSpellEffect(BoostEffect.target(1, 1));
    }
}
