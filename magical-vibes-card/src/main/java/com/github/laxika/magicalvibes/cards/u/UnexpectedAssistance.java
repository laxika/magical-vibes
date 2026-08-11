package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "ECL", collectorNumber = "80")
public class UnexpectedAssistance extends Card {

    public UnexpectedAssistance() {
        // Draw three cards, then discard a card.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
        addEffect(EffectSlot.SPELL, new DiscardEffect(1, DiscardRecipient.CONTROLLER));
    }
}
