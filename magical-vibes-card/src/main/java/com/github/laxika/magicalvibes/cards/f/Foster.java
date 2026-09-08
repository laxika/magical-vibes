package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCardPredicateToHandRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "MMQ", collectorNumber = "247")
public class Foster extends Card {

    public Foster() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES,
                new MayPayManaEffect("{1}",
                        new RevealUntilCardPredicateToHandRestToGraveyardEffect(
                                new CardTypePredicate(CardType.CREATURE)),
                        "Pay {1} to reveal cards until you reveal a creature card?"));
    }
}
