package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "226")
public class ProfaneMemento extends Card {

    public ProfaneMemento() {
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN_CREATURE, ObjectRelation.OPPONENT_OWNS),
                new GainLifeEffect(1));
    }
}
