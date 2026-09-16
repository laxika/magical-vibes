package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentThenDiscardAndReturnToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MB1", collectorNumber = "30")
public class TheGrandTour extends Card {

    public TheGrandTour() {
        target(TargetFilters.permanent()).addEffect(
                EffectSlot.SPELL, new ExileTargetPermanentThenDiscardAndReturnToBattlefieldEffect());
    }
}
