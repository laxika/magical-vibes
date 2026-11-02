package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "502")
@CardRegistration(set = "DMU", collectorNumber = "28")
public class PrayerOfBinding extends Card {

    public PrayerOfBinding() {
        target(TargetFilters.nonlandPermanentAnOpponentControls(), 0, 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new ExileTargetPermanentUntilSourceLeavesEffect())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(2));
    }
}
