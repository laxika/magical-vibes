package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "62")
@CardRegistration(set = "FIN", collectorNumber = "439")
public class MatoyaArchonElder extends Card {

    public MatoyaArchonElder() {
        addTrigger(Triggers.youScry(), new DrawCardEffect(1));
        addTrigger(Triggers.youSurveil(), new DrawCardEffect(1));
    }
}
