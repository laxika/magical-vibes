package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "332")
@CardRegistration(set = "ANB", collectorNumber = "75")
public class ImmortalPhoenix extends Card {

    public ImmortalPhoenix() {
        // When this creature dies, return it to its owner's hand.
        addTrigger(Triggers.dies(ObjectRelation.THIS), new ReturnSourceCardFromGraveyardToOwnerHandEffect());
    }
}
