package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardExileScope;

/**
 * Return — back half of Never // Return.
 * Sorcery — Aftermath (cast only from your graveyard, then exile): Exile target card from a
 * graveyard. Create a 2/2 black Zombie creature token.
 * Modeled as {@link FlashbackCast} on the back face; the engine routes graveyard casts through
 * {@code Card.graveyardCastHalf()}.
 */
public class Return extends Card {

    public Return() {
        // Exile target card from a graveyard. Create a 2/2 black Zombie creature token.
        addEffect(EffectSlot.SPELL, new ExileGraveyardCardsEffect(1, GraveyardExileScope.TARGET_CARDS_ANY_GRAVEYARD));
        addEffect(EffectSlot.SPELL, CreateTokenEffect.blackZombie(1));
        // Aftermath cost equals this half's mana cost; exile after leaving the stack.
        addCastingOption(new FlashbackCast("{3}{B}"));
    }
}
