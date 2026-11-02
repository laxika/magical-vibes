package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.DiscardCardsAndPutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EXO", collectorNumber = "66")
public class MindMaggots extends Card {

    public MindMaggots() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new DiscardCardsAndPutCountersOnSourceEffect(
                        new CardTypePredicate(CardType.CREATURE), 2, "creature cards"));
    }
}
