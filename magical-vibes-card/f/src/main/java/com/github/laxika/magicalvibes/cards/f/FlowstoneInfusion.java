package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DMU", collectorNumber = "124")
public class FlowstoneInfusion extends Card {

    public FlowstoneInfusion() {
        // Target creature gets +2/-2 until end of turn.
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(2, -2));
    }
}
