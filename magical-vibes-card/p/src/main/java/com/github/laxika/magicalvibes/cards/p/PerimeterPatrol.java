package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

@CardRegistration(set = "BRO", collectorNumber = "188")
public class PerimeterPatrol extends Card {

    public PerimeterPatrol() {
        // Whenever an artifact you control enters, this creature gets +1/+0 until end of turn.
        addEffect(EffectSlot.ON_ALLY_ARTIFACT_ENTERS_BATTLEFIELD, new BoostSelfEffect(1, 0));
    }
}
