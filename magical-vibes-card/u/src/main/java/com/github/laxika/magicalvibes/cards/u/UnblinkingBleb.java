package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FUT", collectorNumber = "45")
public class UnblinkingBleb extends Card {

    public UnblinkingBleb() {
        addMorph("{2}{U}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.ANY),
                new MayEffect(new ScryEffect(2), "Scry 2?"));
    }
}
