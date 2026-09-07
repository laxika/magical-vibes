package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TSP", collectorNumber = "186")
public class VolcanicAwakening extends Card {

    public VolcanicAwakening() {
        target(TargetFilters.land())
                .addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
