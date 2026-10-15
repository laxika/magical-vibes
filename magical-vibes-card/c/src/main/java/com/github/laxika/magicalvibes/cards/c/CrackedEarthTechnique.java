package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLE", collectorNumber = "135")
public class CrackedEarthTechnique extends Card {

    public CrackedEarthTechnique() {
        setAllowSharedTargets(true);

        target(TargetFilters.landYouControl())
                .addEffect(EffectSlot.SPELL, new EarthbendTargetLandEffect(3));
        target(TargetFilters.landYouControl())
                .addEffect(EffectSlot.SPELL, new EarthbendTargetLandEffect(3));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(3));
    }
}
