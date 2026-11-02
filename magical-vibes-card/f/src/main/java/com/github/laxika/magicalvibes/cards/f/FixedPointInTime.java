package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PlanarDiePlaneswalkToChaosEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "582")
public class FixedPointInTime extends Card {

    public FixedPointInTime() {
        addTrigger(Triggers.encounterThis(), new PlanarDiePlaneswalkToChaosEffect());
    }
}
