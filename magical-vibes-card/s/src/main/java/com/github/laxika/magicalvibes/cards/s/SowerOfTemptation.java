package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "88")
@CardRegistration(set = "SLD", collectorNumber = "120")
@CardRegistration(set = "WOC", collectorNumber = "111")
public class SowerOfTemptation extends Card {

    public SowerOfTemptation() {
        // When this creature enters, gain control of target creature for as long
        // as this creature remains on the battlefield. (Flying is auto-loaded.)
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_REMAINS));
    }
}
