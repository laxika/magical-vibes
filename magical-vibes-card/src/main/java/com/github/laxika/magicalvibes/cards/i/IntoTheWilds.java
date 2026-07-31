package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

/**
 * Into the Wilds — at the beginning of your upkeep, look at the top card of your library. If it's a
 * land card, you may put it onto the battlefield.
 */
@CardRegistration(set = "M14", collectorNumber = "180")
public class IntoTheWilds extends Card {

    public IntoTheWilds() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new LookAtTopCardMayPutMatchingOntoBattlefieldEffect(new CardTypePredicate(CardType.LAND)));
    }
}
