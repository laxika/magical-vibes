package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

/**
 * Mind — back half of Spring // Mind.
 * Instant — Aftermath (cast only from your graveyard, then exile): Draw two cards.
 * Modeled as {@link FlashbackCast} on the back face; the engine routes graveyard casts through
 * {@code Card.graveyardCastHalf()}.
 */
public class Mind extends Card {

    public Mind() {
        // Draw two cards.
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        // Aftermath cost equals this half's mana cost; exile after leaving the stack.
        addCastingOption(new FlashbackCast("{4}{U}{U}"));
    }
}
