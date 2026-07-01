package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SeethingSong;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/**
 * Blazing Firesinger // Seething Song (SOS 109).
 * <p>
 * Front face — 2/3 Dwarf Bard with Prepared (auto-loaded from Scryfall keywords) and:
 * "This creature enters prepared." While prepared, a copy of its prepare spell {@link SeethingSong} sits in
 * exile and may be cast; casting it unprepares it.
 */
@CardRegistration(set = "SOS", collectorNumber = "109")
public class BlazingFiresingerSeethingSong extends Card {

    public BlazingFiresingerSeethingSong() {
        SeethingSong prepareSpell = new SeethingSong();
        prepareSpell.setSetCode(getSetCode());
        prepareSpell.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(prepareSpell);

        // This creature enters prepared.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "SeethingSong";
    }
}
