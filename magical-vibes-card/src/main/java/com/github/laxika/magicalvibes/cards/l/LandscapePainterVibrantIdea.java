package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.v.VibrantIdea;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/**
 * Landscape Painter // Vibrant Idea (SOS 56).
 *
 * <p>The front face enters prepared, allowing its stored Vibrant Idea spell to be cast from exile.
 */
@CardRegistration(set = "SOS", collectorNumber = "56")
public class LandscapePainterVibrantIdea extends Card {

    public LandscapePainterVibrantIdea() {
        setBackFaceCard(new VibrantIdea());

        // This creature enters prepared.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "VibrantIdea";
    }
}
