package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.PlaysAdditionalLandEachTurnEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "218")
public class HugsGrislyGuardian extends Card {

    public HugsGrislyGuardian() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTopCardsMayPlayUntilNextTurnEffect(new XValue()));
        addStaticEffect(new PlaysAdditionalLandEachTurnEffect(1));
    }
}
