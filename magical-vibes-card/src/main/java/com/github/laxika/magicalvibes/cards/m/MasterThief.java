package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M12", collectorNumber = "64")
public class MasterThief extends Card {

    public MasterThief() {
        // When this creature enters, gain control of target artifact for as long
        // as you control this creature.
        target(TargetFilters.artifact())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new GainControlOfTargetEffect(ControlDuration.WHILE_SOURCE_ON_BATTLEFIELD));
    }
}
