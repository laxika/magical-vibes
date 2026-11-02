package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.UnleashTheFluxEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "605")
public class UnleashTheFlux extends Card {

    public UnleashTheFlux() {
        addTrigger(Triggers.encounterThis(), new UnleashTheFluxEffect());
    }
}
