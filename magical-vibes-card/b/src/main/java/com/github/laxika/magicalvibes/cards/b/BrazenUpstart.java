package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "169")
public class BrazenUpstart extends Card {

    public BrazenUpstart() {
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                        5, new CardTypePredicate(CardType.CREATURE)));
    }
}
