package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OGW", collectorNumber = "80")
public class UnnaturalEndurance extends Card {

    public UnnaturalEndurance() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 0))
                .addSpellEffect(new RegenerateEffect(true));
    }
}
