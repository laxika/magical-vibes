package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DTK", collectorNumber = "2")
@CardRegistration(set = "MUL", collectorNumber = "1")
@CardRegistration(set = "MUL", collectorNumber = "66")
@CardRegistration(set = "MUL", collectorNumber = "131")
@CardRegistration(set = "CMM", collectorNumber = "11")
@CardRegistration(set = "DMC", collectorNumber = "97")
public class AnafenzaKinTreeSpirit extends Card {

    public AnafenzaKinTreeSpirit() {
        // Whenever another nontoken creature you control enters, bolster 1.
        addTrigger(Triggers.enters(Triggers.NONTOKEN_CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new BolsterEffect(1));
    }
}
