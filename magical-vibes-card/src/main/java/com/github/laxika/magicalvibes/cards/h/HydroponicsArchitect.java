package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallySetRandomLandInLibraryAsBasicIslandEffect;

@CardRegistration(set = "YEOE", collectorNumber = "6")
public class HydroponicsArchitect extends Card {

    public HydroponicsArchitect() {
        addEffect(EffectSlot.ON_ATTACK, new PerpetuallySetRandomLandInLibraryAsBasicIslandEffect());
    }
}
