package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.UnleashTheFluxEffect;

@CardRegistration(set = "WHO", collectorNumber = "605")
public class UnleashTheFlux extends Card {

    public UnleashTheFlux() {
        addEffect(EffectSlot.ENCOUNTER_TRIGGERED, new UnleashTheFluxEffect());
    }
}
