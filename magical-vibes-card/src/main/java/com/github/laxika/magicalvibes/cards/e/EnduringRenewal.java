package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnduringRenewalDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PlayWithOwnHandRevealedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCreatureToOwnerHandEffect;

@CardRegistration(set = "ICE", collectorNumber = "23")
public class EnduringRenewal extends Card {

    public EnduringRenewal() {
        // Play with your hand revealed.
        addEffect(EffectSlot.STATIC, new PlayWithOwnHandRevealedEffect());
        // If you would draw a card, reveal the top card of your library instead. If it's a creature
        // card, put it into your graveyard. Otherwise, draw a card.
        addEffect(EffectSlot.STATIC, new EnduringRenewalDrawReplacementEffect());
        // Whenever a creature is put into your graveyard from the battlefield, return it to your hand.
        addEffect(EffectSlot.ON_ALLY_CREATURE_DIES, new ReturnTriggeringCreatureToOwnerHandEffect());
    }
}
