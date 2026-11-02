package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayReturnMilledPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "209")
public class TownGreeter extends Card {

    public TownGreeter() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MillControllerAndMayReturnMilledPermanentToHandEffect(
                        4,
                        new CardTypePredicate(CardType.LAND),
                        new CardSubtypePredicate(CardSubtype.TOWN),
                        2));
    }
}
