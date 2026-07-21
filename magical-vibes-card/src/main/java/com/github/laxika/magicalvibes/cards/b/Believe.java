package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutCreatureOntoBattlefieldElseToHandEffect;

/**
 * Believe — back half of Reason // Believe.
 * Sorcery — Aftermath (cast only from your graveyard, then exile): Look at the top card of your
 * library. You may put it onto the battlefield if it's a creature card. If you don't, put it into
 * your hand.
 * Modeled as {@link FlashbackCast} on the back face; the engine routes graveyard casts through
 * {@code Card.graveyardCastHalf()}.
 */
public class Believe extends Card {

    public Believe() {
        // Look at top; may put creature onto battlefield, else to hand.
        addEffect(EffectSlot.SPELL, new LookAtTopCardMayPutCreatureOntoBattlefieldElseToHandEffect());
        // Aftermath cost equals this half's mana cost; exile after leaving the stack.
        addCastingOption(new FlashbackCast("{4}{G}"));
    }
}
