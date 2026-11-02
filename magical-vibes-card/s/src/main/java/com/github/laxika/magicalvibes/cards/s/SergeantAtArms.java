package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DOM", collectorNumber = "32")
public class SergeantAtArms extends Card {

    public SergeantAtArms() {
        // Kicker {2}{W}
        addStaticEffect(new KickerEffect("{2}{W}"));
        // When this creature enters, if it was kicked, create two 1/1 white Soldier creature tokens.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(new Kicked(), 
                CreateTokenEffect.whiteSoldier(2)
        ));
    }
}
