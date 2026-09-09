package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ME1", collectorNumber = "122")
public class IceStorm extends Card {

    public IceStorm() {
        target(TargetFilters.land()).addEffect(EffectSlot.SPELL, new DestroyTargetPermanentEffect());
    }
}
