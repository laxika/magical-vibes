package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKM", collectorNumber = "17")
public class GriffnautTracker extends Card {

    public GriffnautTracker() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ExileCardsFromGraveyardEffect(2, 0, true));
    }
}
