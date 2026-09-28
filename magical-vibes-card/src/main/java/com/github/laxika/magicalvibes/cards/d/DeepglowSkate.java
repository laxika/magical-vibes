package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleCountersOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SLD", collectorNumber = "1093")
@CardRegistration(set = "SLD", collectorNumber = "2362")
@CardRegistration(set = "2XM", collectorNumber = "48")
public class DeepglowSkate extends Card {

    public DeepglowSkate() {
        // When this creature enters, double the number of each kind of counter on any number
        // of target permanents.
        target(TargetFilters.permanent(), 0, 99)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DoubleCountersOnTargetPermanentEffect());
    }
}
