package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "34")
@CardRegistration(set = "DDI", collectorNumber = "15")
@CardRegistration(set = "TSR", collectorNumber = "60")
public class CrypticAnnelid extends Card {

    public CrypticAnnelid() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(1), new ScryEffect(2), new ScryEffect(3));
    }
}
