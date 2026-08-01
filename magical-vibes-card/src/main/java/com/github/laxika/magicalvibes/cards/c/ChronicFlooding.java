package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "RTR", collectorNumber = "32")
public class ChronicFlooding extends Card {

    public ChronicFlooding() {
        target(TargetFilters.land());
        // Whenever enchanted land becomes tapped, its controller mills three cards.
        addEffect(EffectSlot.ON_ENCHANTED_PERMANENT_TAPPED,
                new MillEffect(3, MillRecipient.TARGET_PLAYER));
    }
}
