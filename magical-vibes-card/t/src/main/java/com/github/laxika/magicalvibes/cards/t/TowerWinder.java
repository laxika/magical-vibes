package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryAndOrGraveyardForCardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTC", collectorNumber = "34")
@CardRegistration(set = "OTC", collectorNumber = "70")
public class TowerWinder extends Card {

    public TowerWinder() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new SearchLibraryAndOrGraveyardForCardToHandEffect(
                        new CardNamedPredicate("Command Tower")));
    }
}
