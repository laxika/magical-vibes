package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RAV", collectorNumber = "155")
@CardRegistration(set = "IMA", collectorNumber = "156")
@CardRegistration(set = "TDC", collectorNumber = "250")
public class CarvenCaryatid extends Card {

    public CarvenCaryatid() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());
    }
}
