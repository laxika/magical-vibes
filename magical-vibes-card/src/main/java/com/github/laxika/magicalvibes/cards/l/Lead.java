package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Lead — back half of Destined // Lead.
 * Sorcery — Aftermath (cast only from your graveyard, then exile): All creatures able to block
 * target creature this turn do so.
 * Modeled as {@link FlashbackCast} on the back face; the engine routes graveyard casts through
 * {@code Card.graveyardCastHalf()}.
 */
public class Lead extends Card {

    public Lead() {
        // All creatures able to block target creature this turn do so.
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new MustBeBlockedByAllCreaturesThisTurnEffect());
        // Aftermath cost equals this half's mana cost; exile after leaving the stack.
        addCastingOption(new FlashbackCast("{3}{G}"));
    }
}
