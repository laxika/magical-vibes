package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RTR", collectorNumber = "61")
public class DarkRevenant extends Card {

    public DarkRevenant() {
        // Flying is auto-loaded from Scryfall.
        // "When this creature dies, put it on top of its owner's library."
        addTrigger(Triggers.dies(ObjectRelation.THIS), new PutSourceCardFromGraveyardIntoLibraryNFromTopEffect(0));
    }
}
