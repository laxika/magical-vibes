package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RampantGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

/** Studious First-Year // Rampant Growth (SOS 162). */
@CardRegistration(set = "SOS", collectorNumber = "162")
public class StudiousFirstYearRampantGrowth extends Card {

    public StudiousFirstYearRampantGrowth() {
        setBackFaceCard(new RampantGrowth());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "RampantGrowth";
    }
}
