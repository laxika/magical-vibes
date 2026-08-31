package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

@CardRegistration(set = "ONS", collectorNumber = "189")
public class BlisteringFirecat extends Card {

    public BlisteringFirecat() {
        addMorph("{R}{R}");
        addEffect(EffectSlot.END_STEP_TRIGGERED, new SacrificeSelfEffect());
    }
}
