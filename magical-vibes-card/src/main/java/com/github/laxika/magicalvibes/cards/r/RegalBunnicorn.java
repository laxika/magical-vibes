package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "WOE", collectorNumber = "25")
public class RegalBunnicorn extends Card {

    public RegalBunnicorn() {
        PermanentCount nonlandPermanentsYouControl = new PermanentCount(
                new PermanentNotPredicate(new PermanentIsLandPredicate()), CountScope.CONTROLLER);
        addEffect(EffectSlot.STATIC,
                new SetPowerToughnessToAmountEffect(nonlandPermanentsYouControl, nonlandPermanentsYouControl));
    }
}
