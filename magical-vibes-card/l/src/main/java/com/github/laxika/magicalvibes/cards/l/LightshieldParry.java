package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DFT", collectorNumber = "19")
public class LightshieldParry extends Card {

    public LightshieldParry() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, 2));

        addCycling("{2}");
    }
}
