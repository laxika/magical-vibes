package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;

@CardRegistration(set = "MH1", collectorNumber = "150")
@CardRegistration(set = "SOC", collectorNumber = "257")
public class ThroesOfChaos extends Card {

    public ThroesOfChaos() {
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());
        addCastingOption(new Retrace());
    }
}
