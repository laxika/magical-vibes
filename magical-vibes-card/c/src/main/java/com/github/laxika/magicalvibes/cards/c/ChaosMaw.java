package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOU", collectorNumber = "87")
public class ChaosMaw extends Card {

    public ChaosMaw() {
        // When this creature enters, it deals 3 damage to each OTHER creature (exclude the source).
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MassDamageEffect(3, false, false,
                        new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));
    }
}
