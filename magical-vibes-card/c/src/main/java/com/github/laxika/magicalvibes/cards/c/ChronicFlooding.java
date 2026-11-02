package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RTR", collectorNumber = "32")
public class ChronicFlooding extends Card {

    public ChronicFlooding() {
        target(TargetFilters.land());
        // Whenever enchanted land becomes tapped, its controller mills three cards.
        addTrigger(Triggers.enchantedPermanentBecomesTapped(),
                new MillEffect(3, MillRecipient.TARGET_PLAYER));
    }
}
