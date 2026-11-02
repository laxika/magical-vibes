package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDiscardsOneThenControllerDrawsIfDiscardedEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "96")
public class FanaticOfTheHarrowing extends Card {

    public FanaticOfTheHarrowing() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new EachPlayerDiscardsOneThenControllerDrawsIfDiscardedEffect());
    }
}
