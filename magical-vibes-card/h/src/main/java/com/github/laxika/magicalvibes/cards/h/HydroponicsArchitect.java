package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PerpetuallySetRandomLandInLibraryAsBasicIslandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YEOE", collectorNumber = "6")
public class HydroponicsArchitect extends Card {

    public HydroponicsArchitect() {
        addTrigger(Triggers.attacks(), new PerpetuallySetRandomLandInLibraryAsBasicIslandEffect());
    }
}
