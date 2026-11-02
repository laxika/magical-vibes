package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LCI", collectorNumber = "211")
@CardRegistration(set = "LCI", collectorNumber = "383")
public class SentinelOfTheNamelessCity extends Card {

    public SentinelOfTheNamelessCity() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofMapToken(1));
        addTrigger(Triggers.attacks(), CreateTokenEffect.ofMapToken(1));
    }
}
