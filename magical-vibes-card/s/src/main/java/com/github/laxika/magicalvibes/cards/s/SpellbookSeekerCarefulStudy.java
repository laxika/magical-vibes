package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.c.CarefulStudy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/**
 * Spellbook Seeker // Careful Study (SOS 68).
 */
@CardRegistration(set = "SOS", collectorNumber = "68")
public class SpellbookSeekerCarefulStudy extends Card {

    public SpellbookSeekerCarefulStudy() {
        setBackFaceCard(new CarefulStudy());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "CarefulStudy";
    }
}
