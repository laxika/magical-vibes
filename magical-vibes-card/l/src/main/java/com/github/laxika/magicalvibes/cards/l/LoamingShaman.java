package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M20", collectorNumber = "180")
@CardRegistration(set = "DIS", collectorNumber = "87")
@CardRegistration(set = "RVR", collectorNumber = "149")
@CardRegistration(set = "C15", collectorNumber = "190")
@CardRegistration(set = "SCD", collectorNumber = "197")
public class LoamingShaman extends Card {

    public LoamingShaman() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ShuffleTargetCardsFromGraveyardIntoLibraryEffect(null, Integer.MAX_VALUE));
    }
}
