package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMatchingToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ORI", collectorNumber = "185")
@CardRegistration(set = "FUT", collectorNumber = "130")
@CardRegistration(set = "DDU", collectorNumber = "18")
public class LlanowarEmpath extends Card {

    public LlanowarEmpath() {
        // When this creature enters, scry 2, then reveal the top card of your library.
        // If it's a creature card, put it into your hand.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ScryEffect(2));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new RevealTopCardMatchingToHandEffect(new CardTypePredicate(CardType.CREATURE)));
    }
}
