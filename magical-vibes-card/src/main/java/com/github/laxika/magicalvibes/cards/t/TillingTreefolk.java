package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "EVE", collectorNumber = "78")
public class TillingTreefolk extends Card {

    public TillingTreefolk() {
        // When this creature enters, you may return up to two target land cards
        // from your graveyard to your hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnTargetCardsFromGraveyardToHandEffect(
                new CardTypePredicate(CardType.LAND), 2));
    }
}
