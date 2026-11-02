package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SHM", collectorNumber = "229")
@CardRegistration(set = "MMA", collectorNumber = "190")
@CardRegistration(set = "UMA", collectorNumber = "216")
@CardRegistration(set = "MB2", collectorNumber = "85")
public class KitchenFinks extends Card {

    public KitchenFinks() {
        // Persist is loaded from Scryfall and handled by PermanentRemovalService.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(2));
    }
}
