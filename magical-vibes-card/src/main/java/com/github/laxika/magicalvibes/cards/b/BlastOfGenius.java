package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "DGM", collectorNumber = "55")
public class BlastOfGenius extends Card {

    public BlastOfGenius() {
        // Choose any target. Draw three cards, then discard a card. Blast of Genius deals damage
        // equal to the discarded card's mana value to that permanent or player.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
        addEffect(EffectSlot.SPELL, new DiscardEffect(1, DiscardRecipient.CONTROLLER));
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(new LastDiscardedCardManaValue()));
    }
}
