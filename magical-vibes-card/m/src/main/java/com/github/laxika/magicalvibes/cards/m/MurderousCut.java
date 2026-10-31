package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DelveCost;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KTK", collectorNumber = "81")
public class MurderousCut extends Card {

    public MurderousCut() {
        addCastingCost(new DelveCost());
        target(TargetFilters.creature())
                .addSpellEffect(new DestroyTargetPermanentEffect());
    }
}
