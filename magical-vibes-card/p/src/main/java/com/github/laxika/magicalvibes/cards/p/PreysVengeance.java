package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ROE", collectorNumber = "205")
@CardRegistration(set = "IMA", collectorNumber = "182")
public class PreysVengeance extends Card {

    public PreysVengeance() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 2));
    }
}
