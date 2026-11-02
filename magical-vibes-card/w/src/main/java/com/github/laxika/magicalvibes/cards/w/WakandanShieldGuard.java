package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "613")
public class WakandanShieldGuard extends Card {

    public WakandanShieldGuard() {
        // When this creature enters, create a 1/1 white Soldier creature token.
        addTrigger(Triggers.enters(ObjectRelation.THIS), CreateTokenEffect.whiteSoldier(1));
    }
}
