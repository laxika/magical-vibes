package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DiscardUpToThenDrawThatManyEffect;

/**
 * Chance — back half of Leave // Chance.
 * Sorcery — Aftermath (cast only from your graveyard, then exile): Discard any number of cards,
 * then draw that many cards.
 * Modeled as {@link FlashbackCast} on the back face; the engine routes graveyard casts through
 * {@code Card.graveyardCastHalf()}.
 */
public class Chance extends Card {

    public Chance() {
        // Discard any number of cards, then draw that many cards.
        addEffect(EffectSlot.SPELL, new DiscardUpToThenDrawThatManyEffect(
                DiscardUpToThenDrawThatManyEffect.ANY_NUMBER));
        // Aftermath cost equals this half's mana cost; exile after leaving the stack.
        addCastingOption(new FlashbackCast("{3}{R}"));
    }
}
