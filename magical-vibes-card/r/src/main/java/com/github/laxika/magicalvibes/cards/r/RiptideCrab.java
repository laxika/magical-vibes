package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "INV", collectorNumber = "266")
@CardRegistration(set = "MB1", collectorNumber = "228")
public class RiptideCrab extends Card {

    public RiptideCrab() {
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DrawCardEffect());
    }
}
