package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ControllerExperienceCounters;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLE", collectorNumber = "145")
@CardRegistration(set = "TLE", collectorNumber = "209")
public class TophEarthbendingMaster extends Card {

    public TophEarthbendingMaster() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD, new ExperienceCountersEffect(1));

        target(TargetFilters.landYouControl())
                .addEffect(EffectSlot.ON_ALLY_CREATURES_ATTACK,
                        new EarthbendTargetLandEffect(new ControllerExperienceCounters()));
    }
}
