package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

/**
 * Rubble — back half of Reduce // Rubble.
 * Sorcery — Aftermath (cast only from your graveyard, then exile): Up to three target lands don't
 * untap during their controller's next untap step.
 * Modeled as {@link FlashbackCast} on the back face; the engine routes graveyard casts through
 * {@code Card.graveyardCastHalf()}.
 */
public class Rubble extends Card {

    public Rubble() {
        // Up to three target lands don't untap during their controller's next untap step.
        target(TargetFilters.land(), 0, 3)
                .addEffect(EffectSlot.SPELL, new SkipNextUntapEffect(TapUntapScope.TARGET));

        // Aftermath cost equals this half's mana cost; exile after leaving the stack.
        addCastingOption(new FlashbackCast("{2}{R}"));
    }
}
