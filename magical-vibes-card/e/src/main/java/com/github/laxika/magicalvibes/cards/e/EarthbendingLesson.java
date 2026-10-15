package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "176")
public class EarthbendingLesson extends Card {

    public EarthbendingLesson() {
        target(TargetFilters.landYouControl())
                .addEffect(EffectSlot.SPELL, new EarthbendTargetLandEffect(4));
    }
}
