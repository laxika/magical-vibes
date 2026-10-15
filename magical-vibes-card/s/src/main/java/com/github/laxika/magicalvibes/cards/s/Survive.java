package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.EachPlayerShufflesGraveyardIntoLibraryEffect;

/**
 * Survive — back half of Struggle // Survive.
 * Sorcery — Aftermath (cast only from your graveyard, then exile): Each player shuffles their
 * graveyard into their library.
 * Modeled as {@link FlashbackCast} on the back face; the engine routes graveyard casts through
 * {@code Card.graveyardCastHalf()}.
 */
public class Survive extends Card {

    public Survive() {
        // Each player shuffles their graveyard into their library.
        addEffect(EffectSlot.SPELL, new EachPlayerShufflesGraveyardIntoLibraryEffect());
        // Aftermath cost equals this half's mana cost; exile after leaving the stack.
        addCastingOption(new FlashbackCast("{1}{G}"));
    }
}
