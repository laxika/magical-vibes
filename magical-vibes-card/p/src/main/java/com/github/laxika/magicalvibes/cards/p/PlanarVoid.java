package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "149")
public class PlanarVoid extends Card {

    public PlanarVoid() {
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN, ObjectRelation.ANOTHER_YOU_OWN),
                ExileGraveyardCardsEffect.cards(Subjects.triggeringCards()));
        addTrigger(Triggers.putIntoGraveyardFromAnywhere(Triggers.NONTOKEN, ObjectRelation.OPPONENT_OWNS),
                ExileGraveyardCardsEffect.cards(Subjects.triggeringCards()));
    }
}
