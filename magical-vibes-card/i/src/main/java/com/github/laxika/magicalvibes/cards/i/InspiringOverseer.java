package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "18")
@CardRegistration(set = "FDN", collectorNumber = "496")
@CardRegistration(set = "SLD", collectorNumber = "1523")
@CardRegistration(set = "BLC", collectorNumber = "141")
@CardRegistration(set = "FDC", collectorNumber = "35")
public class InspiringOverseer extends Card {

    public InspiringOverseer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(1), new DrawCardEffect(1));
    }
}
