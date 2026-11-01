package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardToOwnerHandEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardSupertypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BOK", collectorNumber = "30")
public class YomijiWhoBarsTheWay extends Card {

    public YomijiWhoBarsTheWay() {
        // Whenever a legendary permanent other than Yomiji is put into a graveyard from the
        // battlefield, return that card to its owner's hand. Yomiji has already left the
        // battlefield when its own death is processed, so "other than Yomiji" needs no extra gate.
        addTrigger(Triggers.dies(null, ObjectRelation.ANY),
                new TriggeringCardConditionalEffect(
                        new CardSupertypePredicate(CardSupertype.LEGENDARY),
                        new ReturnTriggeringCardToOwnerHandEffect()));
    }
}
