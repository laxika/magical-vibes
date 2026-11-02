package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReorderTopCardsOfLibraryEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LRW", collectorNumber = "70")
@CardRegistration(set = "DDT", collectorNumber = "8")
public class InkfathomDivers extends Card {

    public InkfathomDivers() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ReorderTopCardsOfLibraryEffect(4));
    }
}
