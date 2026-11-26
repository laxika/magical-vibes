package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "8ED", collectorNumber = "198")
@CardRegistration(set = "WTH", collectorNumber = "109")
public class LavaHounds extends Card {

    public LavaHounds() {
        // Haste is auto-loaded from Scryfall.
        // When this creature enters, it deals 4 damage to you.
        addTrigger(Triggers.enters(ObjectRelation.THIS), DealDamageEffect.to(Subjects.you(), 4));
    }
}
