package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "258")
public class Pyknite extends Card {

    public Pyknite() {
        // "When this creature enters, draw a card at the beginning of the next turn's upkeep."
        addTrigger(Triggers.enters(ObjectRelation.THIS), new RegisterDrawCardsAtNextUpkeepEffect());
    }
}
