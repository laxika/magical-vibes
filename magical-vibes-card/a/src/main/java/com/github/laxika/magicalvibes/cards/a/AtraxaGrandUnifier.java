package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsForEachCardTypeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "196")
@CardRegistration(set = "FCA", collectorNumber = "49")
public class AtraxaGrandUnifier extends Card {

    public AtraxaGrandUnifier() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new RevealTopCardsForEachCardTypeEffect(10));
    }
}
