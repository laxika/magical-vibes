package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.StrikingPalette;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/**
 * Pigment Wrangler // Striking Palette (SOS 126).
 *
 * <p>The creature enters prepared, allowing its Striking Palette prepare spell to be cast from exile.
 */
@CardRegistration(set = "SOS", collectorNumber = "126")
public class PigmentWranglerStrikingPalette extends Card {

    public PigmentWranglerStrikingPalette() {
        setBackFaceCard(new StrikingPalette());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "StrikingPalette";
    }
}
