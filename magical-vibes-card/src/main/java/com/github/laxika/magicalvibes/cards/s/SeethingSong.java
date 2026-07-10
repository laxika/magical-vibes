package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

/**
 * Seething Song — the prepare spell (inset) of Blazing Firesinger // Seething Song (SOS 109).
 * <p>
 * Instant: Add RRRRR.
 * <p>
 * Not independently registered: its oracle data is registered for the class name "SeethingSong" when
 * Blazing Firesinger (SOS 109) loads (see {@code BlazingFiresingerSeethingSong#getBackFaceClassName}). A copy of
 * this spell is created in exile while Blazing Firesinger is prepared and may be cast from there.
 */
@CardRegistration(set = "9ED", collectorNumber = "216")
public class SeethingSong extends Card {

    public SeethingSong() {
        addEffect(EffectSlot.SPELL, new AwardManaEffect(ManaColor.RED, 5));
    }
}
