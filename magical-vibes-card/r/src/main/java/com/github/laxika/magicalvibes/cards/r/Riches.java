package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesCreatureYouGainControlEffect;

/**
 * Riches — back half of Rags // Riches.
 * Sorcery — Aftermath (cast only from your graveyard, then exile): Each opponent chooses a
 * creature they control. You gain control of those creatures.
 * Modeled as {@link FlashbackCast} on the back face; the engine routes graveyard casts through
 * {@code Card.graveyardCastHalf()}.
 */
public class Riches extends Card {

    public Riches() {
        // Each opponent chooses a creature they control. You gain control of those creatures.
        addEffect(EffectSlot.SPELL, new EachOpponentChoosesCreatureYouGainControlEffect());

        // Aftermath cost equals this half's mana cost; exile after leaving the stack.
        addCastingOption(new FlashbackCast("{5}{U}{U}"));
    }
}
