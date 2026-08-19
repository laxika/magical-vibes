package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentMayCastWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RIX", collectorNumber = "46")
public class ReleaseToTheWind extends Card {

    public ReleaseToTheWind() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentMayCastWithoutPayingManaCostEffect());
    }
}
