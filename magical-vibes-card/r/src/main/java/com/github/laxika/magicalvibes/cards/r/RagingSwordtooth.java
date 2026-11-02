package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "226")
@CardRegistration(set = "LCC", collectorNumber = "283")
public class RagingSwordtooth extends Card {

    public RagingSwordtooth() {
        // When this creature enters, it deals 1 damage to each other creature.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MassDamageEffect(1, false, false,
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
    }
}
