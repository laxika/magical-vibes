package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

/**
 * Market — back half of Farm // Market.
 * Sorcery — Aftermath (cast only from your graveyard, then exile): Draw two cards, then discard two cards.
 * Modeled as {@link FlashbackCast} on the back face; the engine routes graveyard casts through
 * {@code Card.graveyardCastHalf()}.
 */
public class Market extends Card {

    public Market() {
        // Draw two cards, then discard two cards.
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(new DiscardEffect(2, DiscardRecipient.CONTROLLER));
        // Aftermath cost equals this half's mana cost; exile after leaving the stack.
        addCastingOption(new FlashbackCast("{2}{U}"));
    }
}
