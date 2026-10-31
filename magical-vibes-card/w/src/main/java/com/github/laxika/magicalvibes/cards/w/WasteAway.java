package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TOR", collectorNumber = "88")
public class WasteAway extends Card {

    public WasteAway() {
        addCastingCost(new DiscardCardTypeCost(null, null));
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(-5, -5));
    }
}
