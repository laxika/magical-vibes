package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M19", collectorNumber = "241")
@CardRegistration(set = "M20", collectorNumber = "232")
@CardRegistration(set = "FDN", collectorNumber = "256")
@CardRegistration(set = "SLD", collectorNumber = "285")
@CardRegistration(set = "SLD", collectorNumber = "1660")
@CardRegistration(set = "SLD", collectorNumber = "2225")
@CardRegistration(set = "CMM", collectorNumber = "399")
public class MeteorGolem extends Card {

    public MeteorGolem() {
        target(TargetFilters.nonlandPermanentAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyTargetPermanentEffect());
    }
}
