package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesHandAndGraveyardIntoLibraryEffect;

/**
 * Memory — back half of Commit // Memory.
 * Sorcery — Aftermath (cast only from your graveyard, then exile): Each player shuffles their hand
 * and graveyard into their library, then draws seven cards.
 * Modeled as {@link FlashbackCast} on the back face; the engine routes graveyard casts through
 * {@code Card.graveyardCastHalf()}.
 */
public class Memory extends Card {

    public Memory() {
        // Each player shuffles their hand and graveyard into their library, then draws seven cards.
        addEffect(EffectSlot.SPELL, new EachPlayerShufflesHandAndGraveyardIntoLibraryEffect());
        addEffect(EffectSlot.SPELL, new EachPlayerDrawsCardEffect(7));
        // Aftermath cost equals this half's mana cost; exile after leaving the stack.
        addCastingOption(new FlashbackCast("{4}{U}{U}"));
    }
}
