package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "167")
@CardRegistration(set = "MBS", collectorNumber = "51")
@CardRegistration(set = "APC", collectorNumber = "49")
@CardRegistration(set = "DDD", collectorNumber = "39")
@CardRegistration(set = "GVL", collectorNumber = "39")
@CardRegistration(set = "IMA", collectorNumber = "102")
@CardRegistration(set = "EMA", collectorNumber = "102")
@CardRegistration(set = "DMU", collectorNumber = "99")
@CardRegistration(set = "DMR", collectorNumber = "99")
@CardRegistration(set = "C15", collectorNumber = "132")
@CardRegistration(set = "MOC", collectorNumber = "265")
@CardRegistration(set = "CMA", collectorNumber = "62")
public class PhyrexianRager extends Card {

    public PhyrexianRager() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(), new LoseLifeEffect(1));
    }
}
