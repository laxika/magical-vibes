package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterRandomDiscardAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TMT", collectorNumber = "88")
@CardRegistration(set = "TMT", collectorNumber = "235")
@CardRegistration(set = "TMT", collectorNumber = "286")
@CardRegistration(set = "TMT", collectorNumber = "296")
public class CaseyJonesVigilante extends Card {

    public CaseyJonesVigilante() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DrawCardEffect(3),
                new RegisterRandomDiscardAtNextUpkeepEffect(3));
    }
}
