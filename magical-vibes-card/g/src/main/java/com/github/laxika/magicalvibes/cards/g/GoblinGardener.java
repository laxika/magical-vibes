package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "7ED", collectorNumber = "188")
@CardRegistration(set = "UDS", collectorNumber = "84")
public class GoblinGardener extends Card {

    public GoblinGardener() {
        // When this creature dies, destroy target land.
        target(TargetFilters.land()).addEffect(EffectSlot.ON_DEATH, new DestroyTargetPermanentEffect());
    }
}
