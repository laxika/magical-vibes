package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "40K", collectorNumber = "152")
public class CanoptekTombSentinel extends Card {

    public CanoptekTombSentinel() {
        target(TargetFilters.nonlandPermanent(), 0, 1)
                .addEffect(EffectSlot.ON_SELF_ENTERS_FROM_GRAVEYARD, new ExileTargetPermanentEffect());

        addUnearth("{7}");
    }
}
