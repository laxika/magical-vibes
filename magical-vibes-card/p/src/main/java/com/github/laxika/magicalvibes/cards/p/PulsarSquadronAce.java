package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "28")
public class PulsarSquadronAce extends Card {

    public PulsarSquadronAce() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                LookAtTopCardsEffect.mayRevealOneToHandRestOnBottomRandom(
                        5, new CardSubtypePredicate(CardSubtype.SPACECRAFT),
                        PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
