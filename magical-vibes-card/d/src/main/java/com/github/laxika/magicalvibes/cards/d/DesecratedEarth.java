package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ZEN", collectorNumber = "86")
public class DesecratedEarth extends Card {

    public DesecratedEarth() {
        target(TargetFilters.land())
                .addSpellEffect(new DestroyTargetPermanentEffect())
                .addSpellEffect(new DiscardEffect(1, DiscardRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
