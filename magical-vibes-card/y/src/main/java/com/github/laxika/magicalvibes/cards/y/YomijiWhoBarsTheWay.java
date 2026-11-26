package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOK", collectorNumber = "30")
public class YomijiWhoBarsTheWay extends Card {

    public YomijiWhoBarsTheWay() {
        // Whenever a legendary permanent other than Yomiji is put into a graveyard from the
        // battlefield, return that card to its owner's hand.
        addTrigger(Triggers.dies(new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY), ObjectRelation.ANOTHER),
                new PutCardsIntoHandEffect(Subjects.triggeringCards()));
    }
}
