package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessSacrificeOwnPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "183")
public class ScytheTiger extends Card {

    public ScytheTiger() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SacrificeUnlessSacrificeOwnPermanentEffect(
                new PermanentIsLandPredicate(), "a land"));
    }
}
