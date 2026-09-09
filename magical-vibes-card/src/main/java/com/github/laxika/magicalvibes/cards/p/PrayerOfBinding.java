package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FDN", collectorNumber = "502")
public class PrayerOfBinding extends Card {

    public PrayerOfBinding() {
        target(TargetFilters.nonlandPermanentAnOpponentControls(), 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetPermanentUntilSourceLeavesEffect())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(2));
    }
}
