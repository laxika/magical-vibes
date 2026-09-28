package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChaosEnsuesEffect;
import com.github.laxika.magicalvibes.model.effect.RevealPlanarCardsUntilPlaneAndPlaneswalkEffect;

@CardRegistration(set = "MOC", collectorNumber = "60")
public class NornsSeedcore extends Card {

    public NornsSeedcore() {
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, new ChaosEnsuesEffect());
        addEffect(EffectSlot.CHAOS_TRIGGERED, new RevealPlanarCardsUntilPlaneAndPlaneswalkEffect());
    }
}
