package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BFZ", collectorNumber = "161")
public class VolcanicUpheaval extends Card {

    public VolcanicUpheaval() {
        target(TargetFilters.land()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
