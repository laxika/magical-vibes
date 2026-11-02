package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsMatchingToHandRestToBottomEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "APC", collectorNumber = "79")
public class KavuHowler extends Card {

    public KavuHowler() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new RevealTopCardsMatchingToHandRestToBottomEffect(
                4, new CardSubtypePredicate(CardSubtype.KAVU)));
    }
}
