package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantTapDrawToRandomLandInLibraryEffect;

@CardRegistration(set = "YEOE", collectorNumber = "22")
public class AmbassadorOfEvendo extends Card {

    public AmbassadorOfEvendo() {
        addEffect(EffectSlot.ON_ALLY_LAND_ENTERS_BATTLEFIELD,
                new PerpetuallyGrantTapDrawToRandomLandInLibraryEffect());
    }
}
