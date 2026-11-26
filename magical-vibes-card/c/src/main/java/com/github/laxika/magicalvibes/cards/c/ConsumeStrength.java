package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "APC", collectorNumber = "93")
@CardRegistration(set = "DDM", collectorNumber = "74")
public class ConsumeStrength extends Card {

    public ConsumeStrength() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 2));
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(-2, -2));
    }
}
