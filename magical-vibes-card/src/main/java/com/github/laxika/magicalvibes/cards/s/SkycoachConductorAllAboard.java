package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.a.AllAboard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/**
 * Skycoach Conductor // All Aboard (SOS 67).
 *
 * <p>The front face enters prepared, allowing its stored All Aboard spell to be cast from exile.
 */
@CardRegistration(set = "SOS", collectorNumber = "67")
public class SkycoachConductorAllAboard extends Card {

    public SkycoachConductorAllAboard() {
        setBackFaceCard(new AllAboard());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "AllAboard";
    }
}
