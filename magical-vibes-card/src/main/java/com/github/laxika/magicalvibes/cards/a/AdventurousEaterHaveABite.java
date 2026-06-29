package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HaveABite;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/**
 * Adventurous Eater // Have a Bite (SOS 72).
 * <p>
 * Front face — 3/2 Human Warlock with Prepared (auto-loaded from Scryfall keywords) and:
 * "This creature enters prepared." While prepared, a copy of its prepare spell {@link HaveABite} sits in
 * exile and may be cast; casting it unprepares it.
 */
@CardRegistration(set = "SOS", collectorNumber = "72")
public class AdventurousEaterHaveABite extends Card {

    public AdventurousEaterHaveABite() {
        HaveABite prepareSpell = new HaveABite();
        prepareSpell.setSetCode(getSetCode());
        prepareSpell.setCollectorNumber(getCollectorNumber());
        setBackFaceCard(prepareSpell);

        // This creature enters prepared.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "HaveABite";
    }
}
