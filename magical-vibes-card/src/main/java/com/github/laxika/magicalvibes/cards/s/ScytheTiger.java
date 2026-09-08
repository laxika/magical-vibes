package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessSacrificeOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "ZEN", collectorNumber = "183")
public class ScytheTiger extends Card {

    public ScytheTiger() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new SacrificeUnlessSacrificeOwnPermanentEffect(
                new PermanentIsLandPredicate(), "a land"));
    }
}
