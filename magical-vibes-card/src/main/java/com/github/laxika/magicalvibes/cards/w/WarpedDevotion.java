package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "8ED", collectorNumber = "172")
public class WarpedDevotion extends Card {

    public WarpedDevotion() {
        // Whenever a permanent is returned to a player's hand, that player discards a card.
        addEffect(EffectSlot.ON_ANY_PERMANENT_RETURNED_TO_HAND, new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, false));
    }
}
