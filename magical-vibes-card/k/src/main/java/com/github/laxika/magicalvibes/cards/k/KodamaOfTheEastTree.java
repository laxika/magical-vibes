package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentManaValue;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPutOntoBattlefieldBySourcePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLC", collectorNumber = "227")
public class KodamaOfTheEastTree extends Card {

    public KodamaOfTheEastTree() {
        // "If it wasn't put onto the battlefield with this ability" can't change once it entered: the filter.
        addTrigger(Triggers.enters(new PermanentNotPredicate(new PermanentPutOntoBattlefieldBySourcePredicate()),
                        ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(
                        new PutCardToBattlefieldEffect(new CardIsPermanentPredicate(), "permanent")
                                .boundedBy(new TriggeringPermanentManaValue()),
                        "Put a permanent card with equal or lesser mana value from your hand onto the battlefield?"));
    }
}
