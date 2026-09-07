package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MKM", collectorNumber = "198")
@CardRegistration(set = "MKM", collectorNumber = "414")
public class Doppelgang extends Card {

    public Doppelgang() {
        targetExactlyX(TargetFilters.permanent(), 100)
                .addEffect(EffectSlot.SPELL, new CreateTokenCopyOfTargetPermanentEffect(new XValue()));
    }
}
