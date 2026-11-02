package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MID", collectorNumber = "52")
@CardRegistration(set = "DBL", collectorNumber = "52")
public class FalconAbomination extends Card {

    public FalconAbomination() {
        // When this creature enters, create a 2/2 black Zombie creature token with decayed.
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.blackZombieWithDecayed(1));
    }
}
