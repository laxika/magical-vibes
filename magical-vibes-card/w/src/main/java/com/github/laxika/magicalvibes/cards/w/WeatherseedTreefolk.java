package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ULG", collectorNumber = "116")
public class WeatherseedTreefolk extends Card {

    public WeatherseedTreefolk() {
        // When this creature dies, return it to its owner's hand.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ReturnSourceCardFromGraveyardToOwnerHandEffect());
    }
}
