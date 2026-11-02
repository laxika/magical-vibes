package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "190")
public class ThornOfTheBlackRose extends Card {

    public ThornOfTheBlackRose() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new BecomeMonarchEffect());
    }
}
