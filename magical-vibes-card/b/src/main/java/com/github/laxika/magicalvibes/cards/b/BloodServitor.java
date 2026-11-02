package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "252")
@CardRegistration(set = "DBL", collectorNumber = "519")
public class BloodServitor extends Card {

    public BloodServitor() {
        // When this creature enters, create a Blood token.
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.ofBloodToken(1));
    }
}
