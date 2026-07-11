package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "P02", collectorNumber = "61")
public class AbyssalNightstalker extends Card {

    public AbyssalNightstalker() {
        // Whenever this creature attacks and isn't blocked, defending player discards a card.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED, new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, false));
    }
}
