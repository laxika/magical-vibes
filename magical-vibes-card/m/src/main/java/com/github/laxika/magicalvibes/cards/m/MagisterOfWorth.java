package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GraceOrCondemnationEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VMA", collectorNumber = "255")
@CardRegistration(set = "C21", collectorNumber = "223")
public class MagisterOfWorth extends Card {

    public MagisterOfWorth() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GraceOrCondemnationEffect());
    }
}
