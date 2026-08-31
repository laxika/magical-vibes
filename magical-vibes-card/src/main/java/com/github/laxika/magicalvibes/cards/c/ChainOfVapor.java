package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentToHandThenMayCopyEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONS", collectorNumber = "73")
public class ChainOfVapor extends Card {

    public ChainOfVapor() {
        target(TargetFilters.nonlandPermanent())
                .addEffect(EffectSlot.SPELL, new ReturnTargetPermanentToHandThenMayCopyEffect());
    }
}
