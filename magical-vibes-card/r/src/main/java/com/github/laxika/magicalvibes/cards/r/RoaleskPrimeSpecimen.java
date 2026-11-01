package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PayXManaConjureRandomCreatureWithManaValueAndCloakEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YMKM", collectorNumber = "26")
public class RoaleskPrimeSpecimen extends Card {

    public RoaleskPrimeSpecimen() {
        addMorph("{G}{U}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.YOU_CONTROL),
                new PayXManaConjureRandomCreatureWithManaValueAndCloakEffect());
    }
}
