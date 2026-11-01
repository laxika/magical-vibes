package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfCardReturnedFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YMID", collectorNumber = "34")
public class VeteranGhoulcaller extends Card {

    public VeteranGhoulcaller() {
        addTrigger(Triggers.putIntoHandFrom(Zone.GRAVEYARD, null, ObjectRelation.YOU_OWN),
                new ConjureDuplicateOfCardReturnedFromGraveyardToHandEffect());
    }
}
