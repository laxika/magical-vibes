package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "THS", collectorNumber = "197")
public class PharikasMender extends Card {

    public PharikasMender() {
        // When this creature enters, you may return target creature or enchantment card from your graveyard to your hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(ReturnTargetCardsFromGraveyardToHandEffect.exactlyOne(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardTypePredicate(CardType.ENCHANTMENT)))),
                "Return the target card to your hand?"));
    }
}
