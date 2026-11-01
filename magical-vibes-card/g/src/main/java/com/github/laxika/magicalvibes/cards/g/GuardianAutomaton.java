package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "227")
public class GuardianAutomaton extends Card {

    public GuardianAutomaton() {
        // When this creature dies, you gain 3 life.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new GainLifeEffect(3));
    }
}
