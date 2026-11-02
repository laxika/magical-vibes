package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "78")
@CardRegistration(set = "LCI", collectorNumber = "405")
public class SpyglassSiren extends Card {

    public SpyglassSiren() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofMapToken(1));
    }
}
