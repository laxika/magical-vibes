package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateToHandRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "247")
@CardRegistration(set = "C13", collectorNumber = "146")
public class Foster extends Card {

    public Foster() {
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL),
                new MayPayManaEffect("{1}",
                        new RevealUntilCardPredicateToHandRestToGraveyardEffect(
                                new CardTypePredicate(CardType.CREATURE)),
                        "Pay {1} to reveal cards until you reveal a creature card?"));
    }
}
