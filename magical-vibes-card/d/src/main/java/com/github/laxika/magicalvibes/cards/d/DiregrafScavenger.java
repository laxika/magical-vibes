package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VOW", collectorNumber = "105")
@CardRegistration(set = "DBL", collectorNumber = "372")
public class DiregrafScavenger extends Card {

    public DiregrafScavenger() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileCardsFromGraveyardEffect(1, new CardTypePredicate(CardType.CREATURE), 2, 2, true));
    }
}
