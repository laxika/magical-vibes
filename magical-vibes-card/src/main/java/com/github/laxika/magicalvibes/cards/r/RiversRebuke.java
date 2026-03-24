package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsTargetPlayerControlsToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "XLN", collectorNumber = "71")
public class RiversRebuke extends Card {

    public RiversRebuke() {
        addEffect(EffectSlot.SPELL, new ReturnPermanentsTargetPlayerControlsToHandEffect(
                new PermanentNotPredicate(new PermanentIsLandPredicate())));
    }
}
