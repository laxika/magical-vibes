package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "TSP", collectorNumber = "74")
public class SageOfEpityr extends Card {

    public SageOfEpityr() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ReorderTopCardsOfLibraryEffect(4));
    }
}
