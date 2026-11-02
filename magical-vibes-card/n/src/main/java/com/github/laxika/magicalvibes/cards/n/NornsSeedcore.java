package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChaosEnsuesEffect;
import com.github.laxika.magicalvibes.model.effect.RevealPlanarCardsUntilPlaneAndPlaneswalkEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOC", collectorNumber = "60")
public class NornsSeedcore extends Card {

    public NornsSeedcore() {
        addTrigger(Triggers.planeswalkToThis(), new ChaosEnsuesEffect());
        addTrigger(Triggers.chaosEnsues(), new RevealPlanarCardsUntilPlaneAndPlaneswalkEffect());
    }
}
