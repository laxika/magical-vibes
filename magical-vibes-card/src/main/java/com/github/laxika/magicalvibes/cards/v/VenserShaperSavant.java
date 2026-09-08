package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellOrPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "FUT", collectorNumber = "46")
public class VenserShaperSavant extends Card {

    public VenserShaperSavant() {
        target(TargetFilters.permanent()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ReturnTargetSpellOrPermanentToHandEffect());
    }
}
