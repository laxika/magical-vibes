package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.ControllerExperienceCounters;
import com.github.laxika.magicalvibes.model.effect.EarthbendTargetLandEffect;
import com.github.laxika.magicalvibes.model.effect.ExperienceCountersEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TLE", collectorNumber = "145")
@CardRegistration(set = "TLE", collectorNumber = "209")
public class TophEarthbendingMaster extends Card {

    public TophEarthbendingMaster() {
        addTrigger(Triggers.enters(Triggers.LAND, ObjectRelation.YOU_CONTROL), new ExperienceCountersEffect(1));

        target(TargetFilters.landYouControl())
                .addTrigger(Triggers.youAttack(),
                        new EarthbendTargetLandEffect(new ControllerExperienceCounters()));
    }
}
